package fi.oamk.petnotes.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.model.PetDataStore
import fi.oamk.petnotes.ui.theme.InputColor
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import fi.oamk.petnotes.viewmodel.PetTagsViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

import fi.oamk.petnotes.ui.theme.CardBG
import fi.oamk.petnotes.ui.theme.LightRed
import fi.oamk.petnotes.ui.theme.LightYellow
import fi.oamk.petnotes.ui.theme.LineColor
import fi.oamk.petnotes.ui.theme.SecondaryColor
import fi.oamk.petnotes.ui.theme.WeightTrend


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
    navController: NavController,
) {
    val context = LocalContext.current
    val isUserLoggedIn = homeScreenViewModel.isUserLoggedIn()
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val userId = homeScreenViewModel.getUserId()

    LaunchedEffect(context) {
        Log.d("HomeScreen", "User logged in: $isUserLoggedIn")
        PetDataStore.getSelectedPetId(context).collect { petId ->
            if (isUserLoggedIn) {
                val fetchedPets = homeScreenViewModel.fetchPets()
                pets = fetchedPets

                //if there is a selected pet in datastore / default :just the first pet
                selectedPet = fetchedPets.find { it.id == petId } ?: fetchedPets.firstOrNull()
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(


                    title = { Text(text = "") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate("addNewPet") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_circle_outline_26),
                                contentDescription = "Add New Pet",
                            )
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
            }
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUserLoggedIn) {
                if (selectedPet != null) {
                    PetCard(selectedPet!!, navController = navController)

                    // Pass the userId to the WeightCard
                    WeightTrendCard(pet = selectedPet!!, userId = userId, navController = navController)
                    // Show the CalendarCard
                    CalendarCard(
                        viewModel = viewModel(), // Make sure viewModel is initialized correctly
                        petId = selectedPet!!.id, // Pass the pet's id
                        navController = navController
                    )


                } else {
                    NoPetsCard(navController)
                }
            } else {
                Text(
                    text = stringResource(R.string.please_log_in_to_continue),
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}



@Composable
fun PetCard(pet: Pet, navController: NavController) {
    Card(
        onClick = { navController.navigate("profile/${pet.id}") },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .padding(top = 10.dp)
            .width(400.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.offset(x=5.dp)) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(InputColor)
                    ) {
                        if (pet.petImageUri.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(pet.petImageUri),
                                contentDescription = "Pet Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(26.dp))

                Column {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pet.name,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
//                            Text(
//                                text = pet.gender,
//                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
//                            )
                            when (pet.gender) {
                                "Male" -> Icon(
                                    imageVector = Icons.Filled.Male,
                                    contentDescription = "Male",
                                    tint = Color.Black
                                )
                                "Female" -> Icon(
                                    imageVector = Icons.Filled.Female,
                                    contentDescription = "Female",
                                    tint = Color.Black
                                )
                                "Other" -> Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Other",
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = pet.specie,
                            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pet.dateOfBirth,
                            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = calculateAge(pet.dateOfBirth),
                            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                .fillMaxWidth()
                .background(LightYellow, shape = RoundedCornerShape(15.dp))
                .padding(horizontal = 20.dp, vertical = 15.dp)) {
                Text(
                    text = "${stringResource(R.string.medical_condition)}: ${pet.medicalCondition}",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Black
                    ),

                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NoPetsCard(navController: NavController) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .padding(top = 150.dp)
            .width(352.dp)
            .height(214.dp),
        onClick = { navController.navigate("addNewPet") }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painter = painterResource(id = R.drawable.baseline_add_circle_outline_26), contentDescription = "Add New Pet")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.add_your_first_pet_to_start), style = TextStyle(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun WeightTrendCard(pet: Pet, userId: String, navController: NavController) {
    // Store the weight entries to plot the trend
    var weightEntries by remember { mutableStateOf<List<Pair<Long, Float>>>(emptyList()) }
    val petWeightLabel = stringResource(R.string.pet_weight)

    // Fetch the weight entries from Firestore
    LaunchedEffect(pet.id) {
        val db = FirebaseFirestore.getInstance()
        try {
            val weightSnapshot = db.collection("pet_weights")
                .whereEqualTo("petId", pet.id)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            val fetchedEntries = weightSnapshot.documents.mapNotNull { doc ->
                val dateString = doc.getString("date")
                val weight = doc.getDouble("weight")?.toFloat()

                // Parse the date string into a Date object if it exists
                val dateMillis = if (dateString != null) {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                    date?.time // Convert to milliseconds
                } else {
                    null
                }

                if (dateMillis != null && weight != null) {
                    Pair(dateMillis, weight)
                } else {
                    null
                }
            }

            // show newest page in WeightScreen
            val pageSize = 7
            val totalItems = fetchedEntries.size
            val lastPage = if (totalItems == 0) 0 else (totalItems - 1) / pageSize
            val startIndex = (lastPage * pageSize).coerceAtLeast(0)
            val endIndex = (startIndex + pageSize).coerceAtMost(totalItems)

            weightEntries = fetchedEntries.subList(startIndex, endIndex)
        } catch (e: Exception) {
            Log.e("WeightTrendCard", "Error fetching weight data", e)
        }
    }

    // If no weight entries, display a "No data" message
    if (weightEntries.isEmpty()) {
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = CardBG),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .padding(20.dp)
                .width(352.dp)
                .clickable {
                    // Navigate to weight screen when clicked
                    navController.navigate("weight_screen/$userId/${pet.id}")
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Add some padding around the box
                contentAlignment = Alignment.Center // Center the content
            ) {
                Text(
                    text = stringResource(R.string.start_adding_your_first_pet_weight_data), // Text to be centered
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

        }
    } else {
        // Process the data for charting
        val chartData = weightEntries.map { (dateMillis, weight) ->
            Pair(dateMillis.toFloat(), weight)
        }
        val dateLabels = weightEntries.map { (dateMillis, _) ->
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(dateMillis))
        }

        // Display the weight trend in a chart
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = CardBG),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .padding(top= 10.dp)
                .width(400.dp)
                .clickable {
                    // Navigate to weight screen when clicked
                    navController.navigate("weight_screen/$userId/${pet.id}")
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Title Text
                Text(
                    stringResource(R.string.weight_trend),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Line Chart
                LineChart(
                    data = remember {
                        listOf(
                            Line(
                                label = petWeightLabel,
                                values = chartData.map { it.second.toDouble() },
                                color = SolidColor(WeightTrend),
                                dotProperties = DotProperties(
                                    enabled = true,
                                    color = SolidColor(Color.White),
                                    strokeColor = SolidColor(WeightTrend)
                                ),
                            )
                        )
                    },
                    modifier = Modifier
                        .width(400.dp)
                        .height(150.dp)
                )

                // Display Date Labels Below Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dateLabels.forEach { dateLabel ->
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier

                        )
                    }
                }
            }
        }
    }
}
@Composable
fun CalendarCard(viewModel: PetTagsViewModel = viewModel(), petId: String, navController: NavController) {
    val currentMonth = remember { YearMonth.now() } // Get the current month
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    LaunchedEffect(petId) {
        viewModel.fetchTagCountsAndDatesFromNotes(petId)
    }

    // Collecting tagDateInfoList from StateFlow
    val tagDateInfoList by viewModel.tagDateInfoList.collectAsState()

    // Ensure all dates are parsed correctly into LocalDate
    val taggedLocalDates = remember(tagDateInfoList) {
        tagDateInfoList.flatMap { tagDateInfo ->
            tagDateInfo.dates.mapNotNull { dateString ->
                try {
                    // Parsing the date string in "yyyy-M-d" format
                    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
                    LocalDate.parse(dateString, formatter)
                } catch (e: Exception) {
                    Log.e("CalendarCard", "Error parsing date: $dateString", e)
                    null
                }
            }
        }.toSet() // Use a set to avoid duplicate dates
    }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val daysOfWeek = remember { daysOfWeek() }

    // Card Layout
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            // Navigate to calendar screen on day click
            navController.navigate("calendarScreen")
        },
        modifier = Modifier
            .width(400.dp)
            .padding(top= 10.dp)
    ) {
        HorizontalCalendar(
            state = state,

            monthHeader = { month ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                ) {
                    // Month and Year
                    Text(
                        text = month.yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()) +
                                " " + month.yearMonth.year,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Weekday headers
                    val adjustedDays = remember(firstDayOfWeek) {
                        daysOfWeek.dropWhile { it != firstDayOfWeek } +
                                daysOfWeek.takeWhile { it != firstDayOfWeek }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        adjustedDays.forEach { dayOfWeek ->
                            Text(
                                text = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },

            monthBody = { _, content ->
                Box(modifier = Modifier.padding(10.dp)) {
                    content()
                }
            },

            monthContainer = { _, container ->
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    container()
                }
            },

            dayContent = { day ->
                val isTagged = day.date in taggedLocalDates
                Day(
                    day = day,
                    isTagged = isTagged, // Only show if tagged
                    currentMonth = currentMonth, // Pass the currentMonth here
                    onClick = { navController.navigate("calendarScreen") } // Add onClick
                )
            }
        )
    }
}


@Composable
fun Day(
    day: CalendarDay,
    isTagged: Boolean = false,
    onClick: (CalendarDay) -> Unit,
    currentMonth: YearMonth // Pass currentMonth to the composable
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                color = if (isTagged) LightRed else Color.Transparent // Highlight tagged days
            )
            .clickable { onClick(day) }, // Make the day clickable
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (day.date.month == currentMonth.month) Color.Black else Color.Gray // Correct comparison for current month
            )
        }
    }
}





fun calculateAge(dateOfBirth: String?): String {
    if (dateOfBirth.isNullOrEmpty()) return "Unknown Age"
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val birthDate = LocalDate.parse(dateOfBirth, formatter)
        val period = Period.between(birthDate, LocalDate.now())
        "${period.years} years and ${period.months} months"
    } catch (e: DateTimeParseException) {
        "Invalid Date Format"
    }
}
