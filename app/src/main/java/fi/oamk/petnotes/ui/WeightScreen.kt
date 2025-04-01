package fi.oamk.petnotes.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(navController: NavController, petId: String, userId: String) {
    val db = FirebaseFirestore.getInstance()

    // UI States
    val weight = remember { mutableStateOf("") }
    val selectedDate = remember { mutableStateOf(Calendar.getInstance().time) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var petName by remember { mutableStateOf<String?>(null) }
    var weightEntries by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch pet name
    LaunchedEffect(petId, userId) {
        try {
            val petDocument = db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .get()
                .await()

            petName = petDocument.getString("name") ?: "Unknown Pet"
        } catch (e: Exception) {
            petName = "Error fetching pet name"
        }
    }

    // Fetch weight data
    LaunchedEffect(petId, userId) {
        try {
            val snapshot = db.collection("pet_weights")
                .whereEqualTo("petId", petId)
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            weightEntries = snapshot.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: return@mapNotNull null
                val weightValue = doc.getDouble("weight")?.toFloat() ?: return@mapNotNull null
                date to weightValue
            }
        } catch (e: Exception) {
            weightEntries = emptyList()
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                text = "Selected Date: $formattedDate",
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
                        selectedDate.value = selectedCalendar.time
                        isDatePickerOpen = false
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            Button(
                onClick = {
                    val weightData = hashMapOf(
                        "userId" to userId,
                        "petId" to petId,
                        "petName" to (petName ?: "Unknown Pet"),
                        "weight" to weight.value.toFloatOrNull(),
                        "date" to formattedDate
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

            // Display the line chart
            if (weightEntries.isNotEmpty()) {
//                PetWeightChart(weightEntries)
            }
        }
    }
}
