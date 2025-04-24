// WeightScreen.kt
package fi.oamk.petnotes.ui

import android.app.DatePickerDialog
import android.hardware.lights.Light
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.derivedStateOf
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
import fi.oamk.petnotes.ui.theme.PrimaryColor
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.DialogProperties
import fi.oamk.petnotes.ui.theme.CardBG
import fi.oamk.petnotes.ui.theme.Default
import fi.oamk.petnotes.ui.theme.LightGrey
import fi.oamk.petnotes.ui.theme.LineColor
import fi.oamk.petnotes.ui.theme.SecondaryColor
import fi.oamk.petnotes.ui.theme.WeightTrend


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

    val scrollState = rememberScrollState()
    val showScrollToTop by remember {
        derivedStateOf { scrollState.value > 200 }
    }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
            //line
            HorizontalDivider(thickness = 1.dp, color = LineColor)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar(navController = navController) },

        floatingActionButton = {
            // Scroll to top floating action button
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            // Smooth scroll to top
                            scrollState.animateScrollTo(0)
                        }
                    },
                    containerColor = PrimaryColor,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 70.dp, end = 16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top"
                    )
                }
            }
        }


    ) { innerPadding ->

//        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
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

            Text(
                text = stringResource(R.string.add_new_weight),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start).padding(start= 30.dp)
            )

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

            Text(
                stringResource(R.string.weight_history),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start).padding(start= 30.dp)
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
        colors = CardDefaults.cardColors(containerColor = CardBG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        tint = if (canScrollLeft) SecondaryColor else Color.Gray
                    )
                }

                Text(
                    stringResource(R.string.weight_trend),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                IconButton(
                    onClick = onScrollRight,
                    enabled = canScrollRight,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = if (canScrollRight) SecondaryColor else Color.Gray
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
                            color = SolidColor(WeightTrend),
                            dotProperties = DotProperties(
                                enabled = true,
                                color = SolidColor(Color.White),
                                strokeColor = SolidColor(WeightTrend),

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
        colors = CardDefaults.cardColors(containerColor = CardBG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
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
            colors = CardDefaults.cardColors(containerColor =CardBG),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .padding(15.dp)
                .width(400.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 30.dp).fillMaxWidth(),
            ) {
                // Date Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Text(
                        text = stringResource(
                            R.string.date,
                            dateFormatforselect.format(selectedDate ?: currentDate)
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-2).dp),

                        )
                    Button(
                        onClick = { isDatePickerOpen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightGrey,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.width(130.dp)
                    ) {
                        Text(stringResource(R.string.select_date), fontWeight = FontWeight.Bold)
                    }
                }

                // Show Date Picker Dialog
                val pickerState = rememberDatePickerState()

                if (isDatePickerOpen) {
                    AlertDialog(
                        onDismissRequest = { isDatePickerOpen = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false),
                        modifier = Modifier.width(400.dp),
                        confirmButton = {
                            TextButton(onClick = {
                                pickerState.selectedDateMillis?.let { millis ->
                                    val selectedDate = Date(millis)
                                    onDateSelected(selectedDate)
                                }
                                isDatePickerOpen = false
                            }) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { isDatePickerOpen = false }) {
                                Text(text = stringResource(id = android.R.string.cancel))
                            }
                        },
                        text = {
                            DatePicker(
                                state = pickerState,
                                colors = DatePickerDefaults.colors(
                                    containerColor               = Default,                                )
                            )
                        }
                    )
                }


                // Weight Input Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()


                ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        OutlinedTextField(
                            value = newWeight,
                            onValueChange = { onWeightChange(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(140.dp)
                                .height(50.dp)
                                .offset(x = (-5).dp),
                    shape = RoundedCornerShape(40.dp)
                        )
                        Text(
                            text = "KG",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        onClick = { addWeight() },
                        modifier = Modifier.width(130.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
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

                Spacer(modifier = Modifier.height(3.dp))
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
            colors = CardDefaults.cardColors(containerColor = CardBG),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .padding(20.dp)
                .width(400.dp)
        ) {
            Column(modifier = Modifier.padding(start = 30.dp,end=30.dp, top = 30.dp)) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    weightEntries.forEach { (date, weight) ->
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
//                            //line
//                            HorizontalDivider(
//                                thickness = 1.dp,
//                                color = LineColor,
//                                modifier = Modifier
//                                    .padding(top=4.dp)
//                                    .width(300.dp)
//                            )
                        }
                    }
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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


