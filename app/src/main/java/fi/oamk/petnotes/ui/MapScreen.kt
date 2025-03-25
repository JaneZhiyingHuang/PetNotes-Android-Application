package fi.oamk.petnotes.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, context: Context) {
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

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
                    // Log the user's location
                    Log.d("MapScreen", "User location: Latitude = ${it.latitude}, Longitude = ${it.longitude}")
                }
            }
        }
    }

    // Set up the CameraPositionState for the map
    val cameraPositionState = rememberCameraPositionState {
        // Default to Oulu (Latitude: 65.0124, Longitude: 25.4682)
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

    // Scaffold with TopBar, BottomBar, and content
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") }, // Title for the Map Screen
                // No back button, just the title
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController) // Reuse BottomNavigationBar for consistency
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // GoogleMap composable
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Display the user's location on the map as a marker
                userLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Your Location"
                    )
                }
            }

            // Show location-related text or message if needed
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
