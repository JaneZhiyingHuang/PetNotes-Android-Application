package fi.oamk.petnotes.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(navController: NavController, petId: String, userId: String) {
    // State to store weight, selected date, and pet name
    val weight = remember { mutableStateOf("") }
    val selectedDate = remember { mutableStateOf(Calendar.getInstance().time) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var petName by remember { mutableStateOf<String?>(null) }

    // Snackbar and coroutine scope
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch pet name from Firestore based on petId and userId
    LaunchedEffect(petId, userId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val petDocument = db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .get()
                .await()

            petName = if (petDocument.exists()) {
                petDocument.getString("name") ?: "Unknown Pet"
            } else {
                "Unknown Pet"
            }
        } catch (e: Exception) {
            petName = "Error fetching pet name"
        }
    }

    // Format the selected date as YYYY-MM-DDThh:mm:ssTZD
    val formattedDate = remember(selectedDate.value) {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(selectedDate.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(" ${petName ?: "Loading..."}", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) } // Add SnackbarHost
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = "Pet ID: $petId",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextField(
                value = weight.value,
                onValueChange = { weight.value = it },
                label = { Text("Enter Pet's Weight") },
                modifier = Modifier.padding(top = 16.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = "Selected Date: $formattedDate", // Use the formatted date
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 16.sp
            )

            Button(
                onClick = { isDatePickerOpen = true },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Select Date")
            }

            if (isDatePickerOpen) {
                DatePickerDialog(
                    LocalContext.current,
                    { _, year, month, dayOfMonth ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth)
                        selectedDate.value = selectedCalendar.time // Update the selected date
                        isDatePickerOpen = false // Close the date picker
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            Button(
                onClick = {
                    val db = FirebaseFirestore.getInstance()
                    val weightData = hashMapOf(
                        "userId" to userId,
                        "petId" to petId,
                        "petName" to (petName ?: "Unknown Pet"),
                        "weight" to weight.value,
                        "date" to formattedDate // Use the formatted date here
                    )

                    db.collection("pet_weights")
                        .add(weightData)
                        .addOnSuccessListener {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Weight added successfully!")
                            }
                        }
                        .addOnFailureListener { e ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save Weight")
            }
        }
    }
}
