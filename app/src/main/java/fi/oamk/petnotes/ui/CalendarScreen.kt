package fi.oamk.petnotes.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import fi.oamk.petnotes.model.Notes
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.model.PetDataStore
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import fi.oamk.petnotes.viewmodel.PetTagsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    context: Context,
    homeScreenViewModel: HomeScreenViewModel,
) {
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val refreshTrigger = remember { mutableIntStateOf(0) }
    val viewModel: PetTagsViewModel = viewModel()

    LaunchedEffect(context, selectedPet) {
        Log.d("CalendarScreen", "User logged in: ${isUserLoggedIn()}")
        PetDataStore.getSelectedPetId(context).collect { petId ->
            if (isUserLoggedIn()) {
                val fetchedPets = homeScreenViewModel.fetchPets()
                pets = fetchedPets

                selectedPet = fetchedPets.find { it.id == petId } ?: fetchedPets.firstOrNull()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar Screen") },
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
                                    refreshTrigger.intValue++ // Trigger refresh
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                if (selectedPet != null) {
                    // Pass petId from selectedPet to CalendarCard and PetTagCountsCard
                    CalendarCard(viewModel, selectedPet!!.id, refreshTrigger.intValue)

                    // Pass petId here
                } else {
                    Text("No pet selected.")
                }
            }
        }
    }
}
@Composable
fun CalendarCard(viewModel: PetTagsViewModel = viewModel(), petId: String, refreshTrigger: Int) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
// 🔄 Fetch tag data when petId or refreshTrigger changes
    LaunchedEffect(petId, refreshTrigger) {
        viewModel.fetchTagCountsAndDatesFromNotes(petId)
    }
    val visibleMonth by remember {
        derivedStateOf { state.firstVisibleMonth.yearMonth }
    }

    // ✅ Log currently visible month
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth }
            .collect { month ->
                Log.d("CalendarCard", "Currently visible month: ${month.yearMonth}")
            }
    }

    // 🔄 Fetch tag data
    LaunchedEffect(petId) {
        viewModel.fetchTagCountsAndDatesFromNotes(petId)
    }

    val tagDateInfoList by viewModel.tagDateInfoList.collectAsState()

    val taggedLocalDates = remember(tagDateInfoList) {
        tagDateInfoList.flatMap { tagDateInfo ->
            tagDateInfo.dates.mapNotNull { dateString ->
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
                    LocalDate.parse(dateString, formatter)
                } catch (e: Exception) {
                    Log.e("CalendarCard", "Error parsing date: $dateString", e)
                    null
                }
            }
        }.toSet()
    }

    // Track selected day
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }

    // 🔄 Fetch notes for selected day
    LaunchedEffect(selectedDay) {
        selectedDay?.let {
            viewModel.fetchNotesForSelectedDay(petId, it.date)
        }
    }

    // 🗓️ Calendar UI
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .padding(20.dp)
            .width(370.dp)
            .height(350.dp)
    ) {
        HorizontalCalendar(
            state = state,
            monthHeader = { month ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                                " " + month.yearMonth.year,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(15.dp))

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
                                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
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
            dayContent = { day ->
                val isTagged = day.date in taggedLocalDates
                Day(
                    day = day,
                    isSelected = selectedDay?.date == day.date && day.position == DayPosition.MonthDate,
                    isTagged = isTagged,
                    onClick = { selectedDay = it },
                    currentMonth = currentMonth
                )
            }
        )
    }

    // 🏷️ Show tag count card
    PetTagCountsCard(viewModel, petId, visibleMonth)

    // 📝 Show notes if applicable
    val selectedNotes = viewModel.selectedNotes.value
    if (selectedDay?.date in taggedLocalDates && selectedNotes.isNotEmpty()) {
        NotesDetailCard(notes = selectedNotes)
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isTagged: Boolean = false,
    onClick: (CalendarDay) -> Unit,
    currentMonth: YearMonth
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                color = when {
                    isSelected -> Color.Blue
                    isTagged -> Color(0xFFFFCD4B) // Yellow highlight for tagged days
                    else -> Color.Transparent
                }
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (day.date.month == currentMonth.month) Color.Black else Color.Gray
            )
        }
    }
}
@Composable
fun PetTagCountsCard(
    viewModel: PetTagsViewModel,
    petId: String,
    visibleMonth: YearMonth
) {
    val tagCounts = viewModel.tagCounts

    // 🔁 Re-fetch tag counts when petId or visibleMonth changes
    LaunchedEffect(petId, visibleMonth) {
        Log.d("PetTagCountsCard", "Fetching tag counts for petId: $petId, visibleMonth: $visibleMonth")
        viewModel.fetchTagCountsFromNotes(petId, visibleMonth)
    }

    LaunchedEffect(tagCounts) {
        Log.d("PetTagCountsCard", "Current tag counts: $tagCounts")
    }

    if (tagCounts.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recorded Tags : ${visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${visibleMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                tagCounts.sortedByDescending { it.count }.forEach { tagCount ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = tagCount.tag)
                        Text(text = "${tagCount.count} times", color = Color.Gray)
                    }
                }
            }
        }
    } else {
        Card(
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "No tags recorded yet.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun NotesDetailCard(
    notes: List<Notes>,
    onEdit: (Notes) -> Unit = {}

) {
    Column(modifier = Modifier.padding(16.dp)) {

        notes.forEach { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            Text(
                                text = note.getFormattedDate(),
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = note.tag,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = note.description,
                            fontSize = 16.sp
                        )

                        // Display photos if available
                        if (note.photoUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Photos:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(note.photoUrls) { photoUrl ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(photoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Pet photo",
                                        modifier = Modifier
                                            .height(120.dp)
                                            .width(120.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }


                            // Display document names if available
                        if (note.documentUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Documents:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                note.documentUrls.forEach { documentUrl ->
                                    val fileName = documentUrl.substringAfterLast("/")
                                    val context = LocalContext.current
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                val intent =
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(documentUrl))
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: ActivityNotFoundException) {
                                                    Toast.makeText(
                                                        context,
                                                        "No application found to open this document",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                            contentDescription = "Document",
                                            tint = Color.Blue,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = fileName,
                                            fontSize = 14.sp,
                                            color = Color.Blue,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Edit icon
                    IconButton(onClick = { onEdit(note) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = Color.Gray
                        )
                    }

                }
            }
        }
    }
}


