// WeightScreen.kt
package fi.oamk.petnotes.ui

import android.app.DatePickerDialog
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fi.oamk.petnotes.R
import fi.oamk.petnotes.viewmodel.WeightViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import fi.oamk.petnotes.model.PetDataStore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    navController: NavController,
    userId: String,
) {
    val viewModel: WeightViewModel = viewModel()
    val homeScreenViewModel: HomeScreenViewModel = viewModel()

    // Correct way to observe ViewModel state with collectAsState or observeAsState for LiveData
    val snackbarMessage by viewModel.snackbarMessage.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var newWeight by remember { mutableStateOf("") }
    val weightEntries by viewModel.weightEntries.collectAsState()

    val context = LocalContext.current
    var pets by remember { mutableStateOf<List<fi.oamk.petnotes.model.Pet>>(emptyList()) }
    var selectedPet by remember { mutableStateOf<fi.oamk.petnotes.model.Pet?>(null) }

    var currentPage by remember { mutableStateOf(0) }

    // Call loadPetData when the screen is launched
    LaunchedEffect(context) {
        PetDataStore.getSelectedPetId(context).collect { storedPetId ->
            if (homeScreenViewModel.isUserLoggedIn()) {
                val fetchedPets = homeScreenViewModel.fetchPets()
                pets = fetchedPets
                selectedPet = fetchedPets.find { it.id == storedPetId } ?: fetchedPets.firstOrNull()
            }
        }
    }

    LaunchedEffect(selectedPet, userId) {
        selectedPet?.let { pet ->
            viewModel.loadPetData(pet.id, userId)
        }
    }

    // defalt: shows the newest page
    LaunchedEffect(weightEntries.size) {
        val totalItems = weightEntries.size
        currentPage = if (totalItems == 0) 0 else (totalItems - 1) / 7
    }

    // Initialize SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    // Show the snackbar if there is a message
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (pets.isNotEmpty()) {
                        SelectedPetDropdown(
                            pets = pets,
                            selectedPet = selectedPet,
                            onPetSelected = { pet ->
                                selectedPet = pet
                                coroutineScope.launch {
                                    PetDataStore.setSelectedPetId(context, pet.id)
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the Weight Trend or NoChartCard based on data availability
            if (weightEntries.isNotEmpty()) {
                val sortedEntries = weightEntries.sortedBy { it.first }

                // handle pages in chart
                val pageSize = 7
                val startIndex = (currentPage * pageSize).coerceAtLeast(0)
                val endIndex = (startIndex + pageSize).coerceAtMost(sortedEntries.size)
                val visibleEntries = sortedEntries.subList(startIndex, endIndex)

                val dateLabels = visibleEntries.map { (date, _) ->
                    SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
                }

                val chartData = visibleEntries.mapIndexed { index, (_, weight) ->
                    index.toFloat() to weight
                }

                // scroll <- or ->
                val canScrollLeft = currentPage > 0
                val canScrollRight = (currentPage + 1) * pageSize < sortedEntries.size

                Spacer(modifier = Modifier.height(20.dp))

                WeightTrendCard(
                    chartData = chartData,
                    dateLabels = dateLabels,
                    canScrollLeft = canScrollLeft,
                    canScrollRight = canScrollRight,
                    onScrollLeft = { if (canScrollLeft) currentPage-- },
                    onScrollRight = { if (canScrollRight) currentPage++ }
                )
            } else {
                NoChartCard()
            }

            // Add Weight Card
            AddWeightCard(
                currentDate = Date(),
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                newWeight = newWeight,
                onWeightChange = { newWeight = it },
                addWeight = {
                    val weightValue = newWeight.toFloatOrNull()
                    if (weightValue != null && weightValue > 0) {
                        selectedPet?.let { pet ->
                            viewModel.addWeight(pet.id, userId, weightValue, selectedDate)

                            //refresh after new weight added
                            viewModel.loadPetData(pet.id, userId)
                        }
                    }
                }
            )

            // Weight History (Scrollable)
            WeightHistoryCard(
                weightEntries = weightEntries,
                deleteWeightEntry = { date ->
                    viewModel.deleteWeightEntry(selectedPet?.id ?: "", userId, date)
                }
            )
        }
    }
}

@Composable
fun WeightTrendCard(
    chartData: List<Pair<Float, Float>>,
    dateLabels: List<String>,
    canScrollLeft: Boolean,
    canScrollRight: Boolean,
    onScrollLeft: () -> Unit,
    onScrollRight: () -> Unit
) {
    val petWeightLabel = stringResource(R.string.pet_weight)

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onScrollLeft,
                    enabled = canScrollLeft,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = if (canScrollLeft) Color.Blue else Color.Gray
                    )
                }

                Text(
                    stringResource(R.string.weight_trend),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onScrollRight,
                    enabled = canScrollRight,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = if (canScrollRight) Color.Blue else Color.Gray
                    )
                }
            }

            // Line Chart
            LineChart(
                data = remember(chartData){
                    listOf(
                        Line(
                            label = petWeightLabel,
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
}

@Composable
fun NoChartCard() {
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
                stringResource(R.string.start_adding_your_first_pet_weight_data),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AddWeightCard(
    currentDate: Date,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    newWeight: String,
    onWeightChange: (String) -> Unit,
    addWeight: () -> Unit
) {
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val dateFormatforselect = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    Box(modifier = Modifier) {
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Date Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(70.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.date,
                            dateFormatforselect.format(selectedDate ?: currentDate)
                        ),
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
                        Text(stringResource(R.string.select_date), fontWeight = FontWeight.Bold)
                    }
                }

                // Show Date Picker Dialog
                if (isDatePickerOpen) {
                    DatePickerDialog(
                        LocalContext.current,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.set(year, month, dayOfMonth)
                            onDateSelected(selectedCalendar.time)
                            isDatePickerOpen = false
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                // Weight Input Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { onWeightChange(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(140.dp)
                            .padding(start = 30.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(40.dp)
                    )
                    Text(
                        text = "KG",
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { addWeight() },
                        modifier = Modifier.padding(start = 33.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD9D9D9),
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(30.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.add),
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Icon",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeightHistoryCard(
    weightEntries: List<Pair<Date, Float>>,
    deleteWeightEntry: (Date) -> Unit
) {
    val dateFormatforselect = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
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
                    stringResource(R.string.weight_history),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(weightEntries.sortedByDescending { it.first }) { (date, weight) ->
                        Row(
                            modifier = Modifier
                                .width(400.dp)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(0.5f)) {
                                Text(
                                    text = dateFormatforselect.format(date),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    modifier = Modifier.weight(0.3f)
                                )
                                Text(
                                    text = "$weight kg",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    modifier = Modifier.weight(0.3f)
                                )
                            }
                            IconButton(
                                onClick = { deleteWeightEntry(date) },
                                modifier = Modifier.offset(y = (-15).dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
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
                    stringResource(R.string.no_weight_data_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}


