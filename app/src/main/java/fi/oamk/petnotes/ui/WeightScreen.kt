package fi.oamk.petnotes.ui

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(navController: NavController, petId: String, userId: String) {
    var petName by remember { mutableStateOf<String?>(null) }
    val weightEntries = remember { mutableStateListOf<Pair<Date, Float>>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val dateFormatforselect = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())


    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault()) // For x-axis labels
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var newWeight by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val currentDate = Date()

    var isDatePickerOpen by remember { mutableStateOf(false) } // Define the state for the date picker

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(petId, userId) {
        try {
            val petDocument = db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petId)
                .get()
                .await()

            petName = petDocument.getString("name") ?: "Unknown Pet"

            val weightDocuments = db.collection("pet_weights")
                .whereEqualTo("userId", userId)
                .whereEqualTo("petId", petId)
                .get()
                .await()

            val parsedWeights = weightDocuments.documents.mapNotNull { doc ->
                val dateString = doc.getString("date") ?: return@mapNotNull null
                val date = try {
                    dateFormat.parse(dateString)
                } catch (e: Exception) {
                    Log.e("WeightScreen", "Error parsing date: $dateString", e)
                    null
                }

                val weight = when (val weightValue = doc.get("weight")) {
                    is Number -> weightValue.toFloat()
                    is String -> weightValue.toFloatOrNull()
                    else -> null
                }

                if (date != null && weight != null) {
                    Pair(date, weight)
                } else {
                    null
                }
            }.sortedBy { it.first }

            weightEntries.clear()
            weightEntries.addAll(parsedWeights)

        } catch (e: Exception) {
            Log.e("WeightScreen", "Error fetching weight data", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error fetching weight data")
            }
        }
    }

    fun addWeight() {
        val weightValue = newWeight.toFloatOrNull()
        if (weightValue == null || weightValue <= 0) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enter a valid weight")
            }
            return
        }

        val dateToSave = selectedDate ?: Date() // Use selected date or current date if not selected
        val currentDate = dateFormat.format(dateToSave)

        val weightData = mapOf(
            "userId" to userId,
            "petId" to petId,
            "date" to currentDate,
            "weight" to weightValue
        )

        coroutineScope.launch {
            try {
                db.collection("pet_weights").add(weightData).await()
                Log.d("WeightScreen", "Added weight entry: $weightData")

                // Refresh weight list
                weightEntries.add(dateToSave to weightValue)
                weightEntries.sortBy { it.first }

                newWeight = "" // Reset input field
                focusManager.clearFocus()

                snackbarHostState.showSnackbar("Weight added successfully!")
            } catch (e: Exception) {
                Log.e("WeightScreen", "Error adding weight", e)
                snackbarHostState.showSnackbar("Failed to add weight")
            }
        }
    }

    fun deleteWeightEntry(date: Date) {
        coroutineScope.launch {
            try {
                // Find and delete the entry from Firebase
                val weightDocument = db.collection("pet_weights")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("petId", petId)
                    .whereEqualTo("date", dateFormat.format(date))
                    .get()
                    .await()

                weightDocument.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Remove from the local list after deletion
                weightEntries.removeAll { it.first == date }

                // Sort by date in descending order (latest first)
                weightEntries.sortByDescending { it.first }

                snackbarHostState.showSnackbar("Weight entry deleted successfully!")
            } catch (e: Exception) {
                Log.e("WeightScreen", "Error deleting weight entry", e)
                snackbarHostState.showSnackbar("Failed to delete weight entry")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = " ${petName ?: "Loading..."}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold  // Makes the text bold
                            ),
                            modifier = Modifier.padding(start = 130.dp)  // Adds padding to the right
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally  // Correct way to set horizontal alignment
        ) {

            // Line Chart for all weight entries
            if (weightEntries.isNotEmpty()) {
                val sortedEntries = weightEntries.sortedBy { it.first }
                val dateLabels = sortedEntries.map { (date, _) -> displayDateFormat.format(date) }

                val chartData = sortedEntries.mapIndexed { index, (_, weight) ->
                    index.toFloat() to weight // Use index for x-axis
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Wrap the chart in a Card with the style you requested
                Card(
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .width(400.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Weight Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LineChart(
                            data = remember {
                                listOf(
                                    Line(
                                        label = "Pet Weight",
                                        values = chartData.map { it.second.toDouble() },
                                        color = SolidColor(Color.Blue),
                                        dotProperties = DotProperties(
                                            enabled = true,
                                            color = SolidColor(Color.White),
                                            strokeColor = SolidColor(Color.Blue)
                                        ),
                                    )
                                )
                            },
                            modifier = Modifier
                                .width(350.dp)
                                .height(200.dp)

                        )
                        // Display Date Labels Below Chart
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dateLabels.forEach { dateLabel ->
                                Text(text = dateLabel, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                }
            }else {
                Card(
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .width(400.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Start adding your first pet weight data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
            ) {
                Card(
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .width(400.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // Aligns items inside Column
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Aligns items vertically
                            horizontalArrangement = Arrangement.spacedBy(70.dp) // Adds spacing between items
                        ) {

                            Text(
                                text = "Date: ${dateFormatforselect.format(selectedDate ?: currentDate)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { isDatePickerOpen = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD9D9D9),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Select Date",
                                    fontWeight = FontWeight.Bold)
                            }


                        }

                        // Show Date Picker Dialog
                        if (isDatePickerOpen) {
                            DatePickerDialog(
                                LocalContext.current,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance()
                                    selectedCalendar.set(year, month, dayOfMonth)
                                    selectedDate = selectedCalendar.time
                                    isDatePickerOpen = false
                                },
                                Calendar.getInstance().get(Calendar.YEAR),
                                Calendar.getInstance().get(Calendar.MONTH),
                                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically, // Aligns items vertically
                            horizontalArrangement = Arrangement.spacedBy(15.dp), // Adds spacing between input and button
                            modifier = Modifier.fillMaxWidth() // Ensures proper layout handling
                        ) {
                            OutlinedTextField(
                                value = newWeight,
                                onValueChange = { newWeight = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(start =30.dp)
                                    .height(50.dp),
                                shape = RoundedCornerShape(40.dp)
                            )
                            Text(
                                text = "KG",
                                fontWeight = FontWeight.Bold
                            )


                            Button(
                                onClick = { addWeight() },
                                modifier = Modifier.padding(start = 33.dp), // Add padding to the start (left) of the button
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD9D9D9),
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(30.dp), // Space between the icon and the text
                                    verticalAlignment = Alignment.CenterVertically // Ensure vertical alignment of icon and text
                                ) {
                                    Text(
                                        text = "Add",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add, // You can replace this with any icon you prefer
                                        contentDescription = "Add Icon",
                                        modifier = Modifier.size(20.dp) // Size of the icon
                                    )

                                }
                            }

                        }

                    }
                }
            }

            // Weight History (Scrollable)
            if (weightEntries.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .width(400.dp)
                ) {
                    Column(modifier = Modifier.padding(30.dp)) {
                        Text(
                            "Weight History",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Using LazyColumn for scrollable weight history
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            // Sort weightEntries by date in descending order (latest first)
                            items(weightEntries.sortedByDescending { it.first }) { (date, weight) ->
                                Row(
                                    modifier = Modifier
                                        .width(400.dp)
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(0.5f)) {
                                        Text(
                                            text = "${dateFormatforselect.format(date)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold, // Makes the text bold
                                                fontSize = 15.sp // Increases the font size
                                            ),
                                            modifier = Modifier.weight(0.3f)
                                        )
                                        Text(
                                            text = "${weight} kg",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold, // Makes the text bold
                                                fontSize = 15.sp // Increases the font size
                                            ),
                                            modifier = Modifier.weight(0.3f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { deleteWeightEntry(date) },
                                        modifier = Modifier.offset(y = (-15).dp) // Adjust the value to move it higher
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                    }
                                }
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = Color.Gray,
                                    modifier = Modifier.offset(y = (-12).dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(20.dp)
                        .width(400.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No weight data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

    }
}