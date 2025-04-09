package fi.oamk.petnotes.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import fi.oamk.petnotes.model.PetStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, context: Context) {
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var petStores by remember { mutableStateOf<List<PetStore>>(emptyList()) }
    var userAddress by remember { mutableStateOf("Loading your location...") }
    var showPetStores by remember { mutableStateOf(false) }  // State to control visibility of pet stores
    var petClinics by remember { mutableStateOf<List<PetStore>>(emptyList()) }  // New state for pet clinics
    var showPetClinics by remember { mutableStateOf(false) }  // State to control visibility of pet clinics

    val apiKey ="AIzaSyBG6bx3kXG5YPinY3O759IAAzdUKH3M7eY"
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    // Replace with your API key

    // Check if the user is logged in
    val isLoggedIn = isUserLoggedIn()

    if (!isLoggedIn) {
        // Show login prompt or redirect to login screen
        Text(text = "Please log in to access the map.", modifier = Modifier.padding(16.dp))
        return
    }

    // Request location permission at runtime
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            locationPermissionGranted = true
        }
    }

    // Get user location if permission is granted
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                    // Fetch the address using reverse geocoding
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]?.getAddressLine(0)
                            userAddress = "Your current location is:\n$address"
                        } else {
                            userAddress = "Address not found."
                        }
                    }
                    Log.d("MapScreen", "User location: Latitude = ${it.latitude}, Longitude = ${it.longitude}")
                }
            }
        }
    }

    // Fetch nearby pet stores when userLocation is set
    LaunchedEffect(userLocation) {
        userLocation?.let {
            Log.d("MapScreen", "Fetching nearby pet stores...")

            petStores = fetchNearbyPetStores(it.latitude, it.longitude, apiKey)
        }
    }

    // Set up the CameraPositionState for the map
    val cameraPositionState = rememberCameraPositionState {
        position = userLocation?.let {
            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(it, 12f)
        } ?: com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(65.0124, 25.4682), 12f)
    }

    // Update camera position when userLocation is set
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(it, 12f)
        }
    }
    // Fetch nearby pet clinics when userLocation is set
    LaunchedEffect(userLocation) {
        userLocation?.let {
            Log.d("MapScreen", "Fetching nearby pet clinics...")
            petClinics = fetchNearbyPetClinics(it.latitude, it.longitude, apiKey)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Display the address instead of latitude and longitude
            Spacer(modifier = Modifier.height(30.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                // Row for parallel buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                ) {
                    Button(
                        onClick = { showPetStores = !showPetStores },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(0.dp)), // Set shape to rectangle (no rounded corners)
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))  // Set button color to #D3D3D3
                    ) {
                        Text("Find Pet Stores", color = Color.Black)  // Set text color to black
                    }
                    Button(
                        onClick = { showPetClinics = !showPetClinics },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(0.dp)), // Set shape to rectangle (no rounded corners)
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3D3D3))  // Set button color to #D3D3D3
                    ) {
                        Text("Find Pet Clinics", color = Color.Black)  // Set text color to black
                    }

                }
                Spacer(modifier = Modifier.height(20.dp))

                // Box for the user address text
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = userAddress,
                        modifier = Modifier
                            .padding(start = 20.dp),
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            // Box for the Google Map
            Box(
                modifier = Modifier
                    .width(400.dp)
                    .height(600.dp)
                    .padding(20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.CenterHorizontally)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    // User location marker
                    userLocation?.let {
                        Marker(
                            state = rememberMarkerState(position = it),
                            title = "Your Location"
                        )
                    }

                    // Conditionally display pet store markers if showPetStores is true
                    if (showPetStores) {
                        petStores.forEach { petStore ->
                            Marker(
                                state = rememberMarkerState(position = petStore.location),
                                title = petStore.name,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            )
                        }
                    }
                    // Conditionally display pet clinic markers if showPetClinics is true
                    if (showPetClinics) {
                        petClinics.forEach { petClinic ->
                            Marker(
                                state = rememberMarkerState(position = petClinic.location),
                                title = petClinic.name,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)  // Use a different color for clinics
                            )
                        }
                    }

                }
            }

            if (userLocation == null) {
                Text(
                    text = "Loading your location...",
                    style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


// Function to check if the user is logged in
fun isUserLoggedIn(): Boolean {
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    Log.d("MapScreen", "User logged in: $isLoggedIn")
    return isLoggedIn
}

// Function to fetch nearby pet stores
suspend fun fetchNearbyPetStores(latitude: Double, longitude: Double, apiKey: String): List<PetStore> {
    return withContext(Dispatchers.IO) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=10000&type=pet_store&key=$apiKey"
        try {
            val response = URL(url).readText()
            val jsonObject = JSONObject(response)
            val results = jsonObject.getJSONArray("results")
            val petStoreList = mutableListOf<PetStore>()

            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val location = result.getJSONObject("geometry").getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                val name = result.getString("name")

                petStoreList.add(PetStore(name, LatLng(lat, lng)))

                // Log the pet store name
                Log.d("MapScreen", "Found pet store: $name")
            }

            petStoreList
        } catch (e: Exception) {
            Log.e("MapScreen", "Error fetching pet stores: ${e.message}")
            emptyList()
        }
    }
}
// Function to fetch nearby pet clinics
suspend fun fetchNearbyPetClinics(latitude: Double, longitude: Double, apiKey: String): List<PetStore> {
    return withContext(Dispatchers.IO) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=10000&type=veterinary_care&key=$apiKey" // Change type to 'vet' for pet clinics
        try {
            val response = URL(url).readText()
            val jsonObject = JSONObject(response)
            val results = jsonObject.getJSONArray("results")
            val petClinicList = mutableListOf<PetStore>()

            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val location = result.getJSONObject("geometry").getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                val name = result.getString("name")

                petClinicList.add(PetStore(name, LatLng(lat, lng)))

                // Log the pet clinic name
                Log.d("MapScreen", "Found pet clinic: $name")
            }

            petClinicList
        } catch (e: Exception) {
            Log.e("MapScreen", "Error fetching pet clinics: ${e.message}")
            emptyList()
        }
    }
}
