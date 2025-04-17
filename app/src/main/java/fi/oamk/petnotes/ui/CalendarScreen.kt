package fi.oamk.petnotes.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
import fi.oamk.petnotes.viewmodel.NotesViewModel
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

    LaunchedEffect(context, refreshTrigger.intValue) {
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
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()  // Ensures the LazyColumn takes up the full available space
                    .padding(16.dp) // Apply padding inside the list
            ) {
                item {
                    if (selectedPet != null) {
                        // Pass petId from selectedPet to CalendarCard and PetTagCountsCard
                        CalendarCard(viewModel, selectedPet!!.id, refreshTrigger.intValue)
                    } else {
                        Text("No pet selected.")
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarCard(
    viewModel: PetTagsViewModel = viewModel(),
    petId: String,
    refreshTrigger: Int
) {
    val notesViewModel: NotesViewModel = viewModel()
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val daysOfWeek = remember { daysOfWeek() }

    // State for calendar
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    // Fetch tag data when petId or refreshTrigger changes
    LaunchedEffect(petId, refreshTrigger) {
        viewModel.fetchTagCountsAndDatesFromNotes(petId)
    }

    val visibleMonth by remember {
        derivedStateOf { state.firstVisibleMonth.yearMonth }
    }

    // Log currently visible month
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleMonth }
            .collect { month ->
                Log.d("CalendarCard", "Currently visible month: ${month.yearMonth}")
            }
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

    // Fetch notes for selected day
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
                        text = month.yearMonth.month.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        ) +
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
                                text = dayOfWeek.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.getDefault()
                                ),
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

    if (selectedDay?.date != null && selectedDay!!.date in taggedLocalDates && selectedNotes.isNotEmpty()) {
        NotesDetailCard(
            notes = selectedNotes,
            onDelete = { note ->
                notesViewModel.deleteNote(
                    note,
                    onSuccess = {
                        Log.d("NotesScreen", "Note deleted successfully")
                    },
                    onFailure = { e ->
                        Log.e("NotesScreen", "Delete failed", e)
                    }
                )
            },
            onEdit = { updatedNote ->
                notesViewModel.updateNote(
                    updatedNote,
                    onSuccess = {
                        Log.d("NotesScreen", "Note updated successfully.")
                    },
                    onFailure = { e ->
                        Log.e("NotesScreen", "Note update failed", e)
                    }
                )
            },
            petId = petId, // Pass petId correctly
            notesViewModel = notesViewModel, // Pass NotesViewModel correctly
          // Pass PetTagsViewModel correctly
        )
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
    var expanded by remember { mutableStateOf(true) } // State to toggle card visibility

    if (tagCounts.isNotEmpty()) {
        // Header with expandable functionality
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded } // Toggle on row click
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recorded Tags : ${visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${visibleMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = expanded,
             // Slide out from top to bottom
        )  {
            Card(
                shape = RoundedCornerShape(15.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Displaying tags sorted by count
                    tagCounts.sortedByDescending { it.count }.forEach { tagCount ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tagCount.tag,
                                fontWeight = FontWeight.Bold  // Make this text bold
                            )
                            Text(
                                text = "${tagCount.count} times",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold  // Make this text bold as well
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Display message when no tags are recorded
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
    onDelete: (Notes) -> Unit,
    onEdit: (Notes) -> Unit,
    petId: String, // Pass petId if you need it for fetching pet tags
    petTagsViewModel: PetTagsViewModel = viewModel(), // Pass PetTagsViewModel as dependency
    notesViewModel: NotesViewModel = viewModel() // Pass NotesViewModel as dependency
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var noteBeingEdited by remember { mutableStateOf<Notes?>(null) }
    var editedDescription by remember { mutableStateOf("") }
    var editedTag by remember { mutableStateOf("") }
    var editedPhotoUris by remember { mutableStateOf(listOf<Uri>()) }
    var editedDocumentUris by remember { mutableStateOf(listOf<Uri>()) }
    var removedPhotoUrls by remember { mutableStateOf(listOf<String>()) }
    var removedDocumentUrls by remember { mutableStateOf(listOf<String>()) }

    // Tags state
    var tags by remember { mutableStateOf(listOf<String>()) }

    // Fetch pet tags based on petId (on the ViewModel level)
    LaunchedEffect(petId) {
        petTagsViewModel.fetchPetTags(
            petId = petId,
            onSuccess = { fetchedTags ->
                tags = fetchedTags
            },
            onFailure = {
                Log.e("NotesDetailCard", "Failed to fetch pet tags")
            }
        )
    }

    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Foldable Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                contentDescription = if (expanded) "Collapse Notes" else "Expand Notes",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NotesDetail",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Notes list with fold animation
        AnimatedVisibility(visible = expanded) {
            Column {
                notes.forEach { note ->
                    Card(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                note.date?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = 10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(0.8f))

                                Text(
                                    note.tag,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .background(Color(0xFFFFCD4B), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.weight(0.1f))
                                Row {
                                    IconButton(onClick = {
                                        noteBeingEdited = note
                                        editedDescription = note.description
                                        editedTag = note.tag
                                        editedPhotoUris = listOf()
                                        editedDocumentUris = listOf()
                                        removedPhotoUrls = listOf()
                                        removedDocumentUrls = listOf()
                                        showEditDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Note",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    note.description,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (note.photoUrls.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Photos:", fontWeight = FontWeight.Bold)
                                note.photoUrls.forEach { photoUrl ->
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .fillMaxWidth()
                                            .height(150.dp)
                                    )
                                }
                            }

                            if (note.documentUrls.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Documents:", fontWeight = FontWeight.Bold)
                                note.documentUrls.forEach { documentUrl ->
                                    val fileName = documentUrl.substringAfterLast("/")
                                    Text(
                                        text = fileName,
                                        style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.Underline),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    // Edit Dialog
    if (showEditDialog && noteBeingEdited != null) {
        EditNoteDialog(
            showDialog = showEditDialog,
            noteToEdit = noteBeingEdited,
            existingPhotoUrls = noteBeingEdited!!.photoUrls,
            editedPhotoUris = editedPhotoUris,
            existingDocumentUrls = noteBeingEdited!!.documentUrls,
            editedDocumentUris = editedDocumentUris,
            removedPhotoUrls = removedPhotoUrls,
            removedDocumentUrls = removedDocumentUrls,
            editedDescription = editedDescription,
            editedTag = editedTag,
            tags = tags,
            onDismiss = { showEditDialog = false },
            onDelete = {
                onDelete(noteBeingEdited!!)
                showEditDialog = false
            },
            onSave = { updatedNote, _, _, _, _ ->
                onEdit(updatedNote)
                showEditDialog = false
            },
            onDescriptionChange = { editedDescription = it },
            onTagChange = { editedTag = it },
            onPhotoUrisChange = { editedPhotoUris = it },
            onDocumentUrisChange = { editedDocumentUris = it },
            onRemovePhotoUrl = { removedPhotoUrls = removedPhotoUrls + it },
            onRemovePhotoUri = { editedPhotoUris = editedPhotoUris - it },
            onRemoveDocumentUrl = { removedDocumentUrls = removedDocumentUrls + it },
            onRemoveDocumentUri = { editedDocumentUris = editedDocumentUris - it },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteDialog(
    showDialog: Boolean,
    noteToEdit: Notes?,
    existingPhotoUrls: List<String>,
    editedPhotoUris: List<Uri>,
    existingDocumentUrls: List<String>,
    editedDocumentUris: List<Uri>,
    removedPhotoUrls: List<String>,
    removedDocumentUrls: List<String>,
    editedDescription: String,
    editedTag: String,
    tags: List<String>,
    onDismiss: () -> Unit,
    onDelete: (Notes) -> Unit,
    onSave: (
        updatedNote: Notes,
        newPhotoUris: List<Uri>,
        newDocumentUris: List<Uri>,
        removedPhotos: List<String>,
        removedDocuments: List<String>
    ) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onPhotoUrisChange: (List<Uri>) -> Unit,
    onDocumentUrisChange: (List<Uri>) -> Unit,
    onRemovePhotoUrl: (String) -> Unit,
    onRemovePhotoUri: (Uri) -> Unit,
    onRemoveDocumentUrl: (String) -> Unit,
    onRemoveDocumentUri: (Uri) -> Unit,
) {
    if (showDialog && noteToEdit != null) {
        BasicAlertDialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) ,
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                val coroutineScope = rememberCoroutineScope()

                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(600.dp),  // Ensuring proper height constraints for the scrollable content
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Edit Note",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    item {
                        // Dropdown for Tag Selection
                        DropdownSelector(
                            selectedValue = editedTag,
                            options = tags.filter { it != "All" },
                            onValueChange = onTagChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Outlined text field for Description
                        OutlinedTextField(
                            value = editedDescription,
                            onValueChange = onDescriptionChange,
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp), // Set a height for the description box
                            singleLine = false
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Photo Viewer
                        if (existingPhotoUrls.isNotEmpty() || editedPhotoUris.isNotEmpty()) {
                            Text("Photos", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(existingPhotoUrls.filter { it !in removedPhotoUrls }) { photoUrl ->
                                    PhotoThumbnail(
                                        url = photoUrl,
                                        onRemove = { onRemovePhotoUrl(photoUrl) }
                                    )
                                }
                                items(editedPhotoUris) { uri ->
                                    PhotoThumbnail(
                                        uri = uri,
                                        onRemove = { onRemovePhotoUri(uri) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Document Viewer
                        if (existingDocumentUrls.isNotEmpty() || editedDocumentUris.isNotEmpty()) {
                            Text("Documents", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                existingDocumentUrls.filter { it !in removedDocumentUrls }.forEach { url ->
                                    DocumentRow(
                                        name = url.substringAfterLast("/"),
                                        onRemove = { onRemoveDocumentUrl(url) }
                                    )
                                }
                                editedDocumentUris.forEach { uri ->
                                    DocumentRow(
                                        name = uri.lastPathSegment ?: "Document",
                                        onRemove = { onRemoveDocumentUri(uri) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Pickers for Photos and Documents
                        val photoPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetMultipleContents()
                        ) { uris: List<Uri> -> onPhotoUrisChange(editedPhotoUris + uris) }

                        val documentPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetMultipleContents()
                        ) { uris: List<Uri> -> onDocumentUrisChange(editedDocumentUris + uris) }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp)
                            ) {
                                Text("Add Photos", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { documentPickerLauncher.launch("*/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                            ) {
                                Text("Add Documents", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Buttons Row for Save, Delete, and Cancel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val updatedNote = noteToEdit.copy(
                                            description = editedDescription,
                                            tag = editedTag,
                                            photoUrls = existingPhotoUrls.filter { it !in removedPhotoUrls },
                                            documentUrls = existingDocumentUrls.filter { it !in removedDocumentUrls }
                                        )
                                        onSave(
                                            updatedNote,
                                            editedPhotoUris,
                                            editedDocumentUris,
                                            removedPhotoUrls,
                                            removedDocumentUrls
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9D9D9))
                            ) {
                                Text("Save", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onDelete(noteToEdit) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Delete", fontSize = 12.sp)
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9D9D9))
                            ) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PhotoThumbnail(url: String? = null, uri: Uri? = null, onRemove: () -> Unit) {
    Box(modifier = Modifier) {
        AsyncImage(
            model = url ?: uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth() // Set a fixed size for the image
                .clip(RoundedCornerShape(8.dp)),
            placeholder = ColorPainter(Color(0xFFE0E0E0))
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).background(Color.White, CircleShape)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
        }
    }
}

@Composable
fun DocumentRow(name: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 14.sp, color = Color.Black, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
        }
    }
}

