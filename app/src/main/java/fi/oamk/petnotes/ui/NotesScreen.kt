package fi.oamk.petnotes.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.lights.Light
import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.Notes
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.model.PetDataStore
import fi.oamk.petnotes.ui.theme.All
import fi.oamk.petnotes.ui.theme.CardBG
import fi.oamk.petnotes.ui.theme.Cough
import fi.oamk.petnotes.ui.theme.DarkGrey
import fi.oamk.petnotes.ui.theme.DarkRed
import fi.oamk.petnotes.ui.theme.Else
import fi.oamk.petnotes.ui.theme.Emotion
import fi.oamk.petnotes.ui.theme.LightGrey
import fi.oamk.petnotes.ui.theme.LightRed
import fi.oamk.petnotes.ui.theme.LightYellow
import fi.oamk.petnotes.ui.theme.LineColor
import fi.oamk.petnotes.ui.theme.NoteBG
import fi.oamk.petnotes.ui.theme.NoteInput
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.ui.theme.Stool
import fi.oamk.petnotes.ui.theme.Vet
import fi.oamk.petnotes.ui.theme.Vomit
import fi.oamk.petnotes.ui.theme.WaterIntake
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import fi.oamk.petnotes.viewmodel.NotesViewModel
import fi.oamk.petnotes.viewmodel.PetTagsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesScreen(
    navController: NavController,
    homeScreenViewModel: HomeScreenViewModel,
    petTagsViewModel: PetTagsViewModel,
    notesViewModel: NotesViewModel
) {
    // Check if the user is logged in
    val isUserLoggedIn = remember { FirebaseAuth.getInstance().currentUser != null }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableStateOf(0.dp) }
    val defaultTag = stringResource(R.string.all)
    var selectedTag by remember { mutableStateOf(defaultTag) }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf(TextFieldValue("")) }
    var userInput by remember { mutableStateOf("") }
    var showDeleteConfirmationDialog by remember { mutableStateOf<String?>(null) }
    var fetchedNotes by remember { mutableStateOf(listOf<Notes>()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val lazyListState = rememberLazyListState()
    // Show scroll to top button when scrolled down
    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    // Date
    val currentDate = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(currentDate.get(Calendar.DAY_OF_MONTH).toString()) }
    var selectedMonth by remember { mutableStateOf((currentDate.get(Calendar.MONTH) + 1).toString()) }
    var selectedYear by remember { mutableStateOf(currentDate.get(Calendar.YEAR).toString()) }

    // Add state variables for note editing
    var noteToEdit by remember { mutableStateOf<Notes?>(null) }
    var editedDescription by remember { mutableStateOf("") }
    var editedTag by remember { mutableStateOf("") }
    var editedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var editedDocumentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingDocumentUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var removedPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var removedDocumentUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteNoteDialog by remember { mutableStateOf<Notes?>(null) }
    var addNoteExpanded by remember { mutableStateOf(true) }
    var viewNotesExpanded by remember { mutableStateOf(true) }

    val defaultTags = listOf(
        stringResource(R.string.all), stringResource(R.string.vomit), stringResource(R.string.stool),
        stringResource(R.string.cough), stringResource(R.string.vet), stringResource(R.string.water_intake), stringResource(R.string.emotion)
    )

    // State to track photo and document URIs
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var documentUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Observe upload state
    val uploadState by notesViewModel.uploadState.collectAsState()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        photoUris = uris
    }

    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        documentUris = uris
    }

    fun getTagColor(tag: String, context: Context): Color {
        return when (tag) {
            context.getString(R.string.all) -> All
            context.getString(R.string.vomit) -> Vomit
            context.getString(R.string.stool) -> Stool
            context.getString(R.string.cough) -> Cough
            context.getString(R.string.vet) -> Vet
            context.getString(R.string.water_intake) -> WaterIntake
            context.getString(R.string.emotion) -> Emotion
            else -> Else // Gray for default/custom tags
        }
    }

    fun mapTagToLocalizedString(tag: String, context: Context): String {
        // Map English tags to resource IDs
        val tagResourceMap = mapOf(
            "all" to R.string.all,
            "vomit" to R.string.vomit,
            "stool" to R.string.stool,
            "cough" to R.string.cough,
            "vet" to R.string.vet,
            "water intake" to R.string.water_intake,
            "emotion" to R.string.emotion
            // Add all other tags you use
        )

        val resourceId = tagResourceMap[tag.lowercase()]

        return if (resourceId != null) {
            context.getString(resourceId)
        } else {
            tag
        }
    }

    fun reverseMapLocalizedTagToStorageFormat(localizedTag: String, context: Context): String {
        val reverseTagMap = mapOf(
            context.getString(R.string.all) to "all",
            context.getString(R.string.vomit) to "vomit",
            context.getString(R.string.stool) to "stool",
            context.getString(R.string.cough) to "cough",
            context.getString(R.string.vet) to "vet",
            context.getString(R.string.water_intake) to "water_intake",
            context.getString(R.string.emotion) to "emotion"
        )

        return reverseTagMap[localizedTag] ?: localizedTag
    }

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            coroutineScope.launch {
                val fetchedPets = homeScreenViewModel.fetchPets() // Fetch pets from Firestore
                if (fetchedPets.isNotEmpty()) {
                    pets = fetchedPets

                    // get selected pet from datastore
                    PetDataStore.getSelectedPetId(context).collect { storedPetId ->
                        selectedPet = fetchedPets.find { it.id == storedPetId } ?: fetchedPets.first()
                    }

                    tags = selectedPet?.tags?.takeIf { it.isNotEmpty() } ?: defaultTags
                }
            }
        }
    }

    LaunchedEffect(selectedPet) {
        tags = selectedPet?.tags?.takeIf { it.isNotEmpty() } ?: defaultTags

        selectedPet?.id?.let { petId ->
            fetchedNotes = notesViewModel.getNotesByPetId(petId)
        }
    }

    // Handle upload state
    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is NotesViewModel.UploadState.Success -> {
                refreshTrigger++

                selectedPet?.id?.let { petId ->
                    fetchedNotes = notesViewModel.getNotesByPetId(petId)
                }

                userInput = ""
                photoUris = emptyList()
                documentUris = emptyList()
                selectedTag = context.getString(R.string.all)

                selectedDate = currentDate.get(Calendar.DAY_OF_MONTH).toString()
                selectedMonth = (currentDate.get(Calendar.MONTH) + 1).toString()
                selectedYear = currentDate.get(Calendar.YEAR).toString()

                Toast.makeText(context,"Note added successfully!", Toast.LENGTH_SHORT).show()
            }
            is NotesViewModel.UploadState.Error -> {
                Toast.makeText(context,"Error: ${state.message ?: "Failed to upload note"}", Toast.LENGTH_LONG).show()
                Log.e("NotesScreen", "Upload error: ${state.message}", state.exception)
            }
            is NotesViewModel.UploadState.Loading -> {
                Log.d("NotesScreen", "Upload in progress...")
            }
            is NotesViewModel.UploadState.Idle -> {
                Log.d("NotesScreen", "Upload state: Idle")
            }
        }
    }

    LaunchedEffect(refreshTrigger, selectedPet) {
        selectedPet?.id?.let { petId ->
            fetchedNotes = notesViewModel.getNotesByPetId(petId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
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
                            lazyListState.animateScrollToItem(0)
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
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item{Spacer(modifier=Modifier.height(8.dp))}

            item {
                Card(
                    modifier = Modifier
                        .padding(6.dp)
                        .width(400.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                    containerColor = CardBG
                    )
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .wrapContentHeight(align = Alignment.Top),
                            maxItemsInEachRow = 6
                        ) {
                            // Always show the "All" tag first
                            FilterChip(
                                selected = selectedTag == stringResource(R.string.all),
                                onClick = { selectedTag = context.getString(R.string.all) },
                                label = { Text(stringResource(R.string.all)) },
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            // Display other tags with delete functionality
                            tags.filter { it != stringResource(R.string.all) }.forEach { tag ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    FilterChip(
                                        selected = selectedTag == tag,
                                        onClick = {
                                            selectedTag = tag
                                        },
                                        label = { Text(tag) },
                                        modifier = Modifier.padding(end = 3.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = getTagColor(tag, context).copy(alpha = 0.2f),
                                            labelColor = getTagColor(tag, context),
                                            selectedContainerColor = getTagColor(tag, context).copy(alpha = 0.3f),
                                            selectedLabelColor = getTagColor(tag, context),
                                        ),
                                        border = BorderStroke(0.dp, Color.Transparent)
                                    )

                                    // Only show delete icon when this specific tag is selected
                                    if (selectedTag == tag) {
                                        IconButton(
                                            onClick = {
                                                showDeleteConfirmationDialog = tag
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Delete Tag",
                                                tint = DarkGrey,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (showDeleteConfirmationDialog != null) {
                                BasicAlertDialog(
                                    onDismissRequest = { showDeleteConfirmationDialog = null }
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        colors = CardDefaults.cardColors(
                                        containerColor = CardBG
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = stringResource(R.string.warning),
                                                color = DarkRed,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.tag_delete_alert,
                                                    showDeleteConfirmationDialog!!
                                                ),
                                                modifier = Modifier.padding(bottom = 16.dp),


                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                Button(
                                                    onClick = {
                                                        val tagToDelete = showDeleteConfirmationDialog
                                                        val updatedTags = tags.toMutableList().apply { remove(tagToDelete) }

                                                        selectedPet?.id?.let { petId ->
                                                            coroutineScope.launch {
                                                                val notesToProcess = fetchedNotes.filter { note ->
                                                                    val localizedTag = notesViewModel.mapStorageFormatToDisplayTag(note.tag, context)
                                                                    localizedTag == tagToDelete
                                                                }

                                                                for (note in notesToProcess) {
                                                                    notesViewModel.deleteNote(
                                                                        note,
                                                                        onSuccess = {
                                                                            fetchedNotes = fetchedNotes.filter { it.id != note.id }
                                                                        },
                                                                        onFailure = { error ->
                                                                            Toast.makeText(context, "Failed to delete note: $error", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    )
                                                                }

                                                                // After deleting the notes, update the pet tags
                                                                petTagsViewModel.updatePetTags(
                                                                    petId,
                                                                    updatedTags,
                                                                    onSuccess = {
                                                                        // Successfully updated the tags
                                                                        tags = updatedTags
                                                                        selectedTag = context.getString(R.string.all)
                                                                        showDeleteConfirmationDialog = null
                                                                    },
                                                                    onFailure = { error ->
                                                                        // Handle error updating tags
                                                                        Toast.makeText(context, "Failed to delete tag: $error", Toast.LENGTH_SHORT).show()
                                                                        showDeleteConfirmationDialog = null
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = DarkRed,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Text(stringResource(R.string.yes))
                                                }

                                                Button(
                                                    onClick = {
                                                        showDeleteConfirmationDialog = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = LightGrey,
                                                        contentColor = Color.Black
                                                    )
                                                ) {
                                                    Text(stringResource(R.string.no))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            FilterChip(
                                selected = false,
                                onClick = { showDialog = true },
                                label = { Text(stringResource(R.string.add2)) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { addNoteExpanded = !addNoteExpanded }
                        .padding(vertical = 8.dp, horizontal = 20.dp)
                ) {
                    Icon(
                        imageVector = if (addNoteExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = if (addNoteExpanded) "Collapse Add Notes Box" else "Expand Add Notes Box",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.add_new_abnormal_behaviors),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                AnimatedVisibility(visible = addNoteExpanded) {
                    Card(
                        modifier = Modifier
                            .width(400.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBG
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp,vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding()

                            ) {
                                // Date Selection
                                val months = listOf("1","2","3","4","5","6", "7","8","9","10","11","12")
                                val years = (2020..2030).map { it.toString() }
                                val displayTag =
                                    if (selectedTag == stringResource(R.string.all)) tags.firstOrNull {
                                        it != context.getString(
                                            R.string.all
                                        )
                                    }
                                        ?: stringResource(R.string.vomit) else selectedTag

                                DateSelector(selectedDate) { selectedDate = it }
                                DropdownSelector(
                                    selectedValue = selectedMonth,
                                    options = months,
                                    onValueChange = { selectedMonth = it },
                                    modifier = Modifier.weight(0.5f)
                                )
                                DropdownSelector(
                                    selectedValue = selectedYear,
                                    options = years,
                                    onValueChange = { selectedYear = it },
                                    modifier = Modifier.weight(0.6f)
                                )
                                DropdownSelector(
                                    selectedValue = selectedTag,
                                    options = tags,
                                    onValueChange = { selectedTag = it },
                                    modifier = Modifier.weight(0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Card(
                                modifier = Modifier
                                    .padding(top=8.dp,bottom=10.dp)
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .width(400.dp),
//                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            ) {
                                TextField(
                                    value = userInput,
                                    onValueChange = { userInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
//                                        .padding(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = NoteInput,
                                        unfocusedContainerColor = NoteInput,
//                                        disabledContainerColor = LightYellow,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                        ),
                                    placeholder = { Text(stringResource(R.string.description_placeholder)) },
                                    singleLine = false
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding( vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryColor,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.add_photos, photoUris.size),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(3.dp))

                                Button(
                                    onClick = { documentPickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryColor,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.add_documents,
                                            documentUris.size
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp

                                    )
                                }



                        }

                            Button(
                                onClick = {
                                    if (selectedPet == null) {
                                        Toast.makeText(
                                            context,
                                            "No pet selected",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    if (userInput.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.description_cannot_be_empty),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    if (selectedTag == context.getString(R.string.all)) {
                                        // Prevent note creation if "All" tag is selected
                                        Toast.makeText(
                                            context,
                                            "Cannot create a note with the 'All' tag.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    // Create a Calendar instance for the timestamp
                                    val calendar = Calendar.getInstance()
                                    calendar.set(
                                        selectedYear.toInt(),
                                        selectedMonth.toInt() - 1,
                                        selectedDate.toInt()
                                    )
                                    val userSelectedTimestamp = calendar.timeInMillis

                                    coroutineScope.launch {
                                        notesViewModel.uploadAndCreateNote(
                                            photoUris = photoUris,
                                            documentUris = documentUris,
                                            selectedPet = selectedPet,
                                            userInput = userInput,
                                            selectedYear = selectedYear.toInt(),
                                            selectedMonth = selectedMonth.toInt(),
                                            selectedDate = selectedDate.toInt(),
                                            selectedTag = selectedTag,
                                            userSelectedTimestamp = userSelectedTimestamp,
                                            context = context
                                        )
                                    }
                                },
                                modifier = Modifier.padding(vertical = 8.dp)
                                    .width(150.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                ),
                                enabled = selectedTag != context.getString(R.string.all) // Disable button if "All" tag is selected
                            ) {
                                Text(
                                    text = stringResource(R.string.confirm),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                if (fetchedNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewNotesExpanded = !viewNotesExpanded }
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                    ) {
                        Icon(
                            imageVector = if (viewNotesExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                            contentDescription = if (viewNotesExpanded) "Collapse Notes" else "Expand Notes",
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.your_pet_s_notes),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Display notes
            items(fetchedNotes.filter { note ->
                val localizedTag = notesViewModel.mapStorageFormatToDisplayTag(note.tag, context)
                selectedTag == context.getString(R.string.all) || localizedTag == selectedTag
            }) { note ->
                AnimatedVisibility(visible = viewNotesExpanded) {
                    NoteCard(
                        note = note,
                        onEdit = {
                            noteToEdit = it
                            editedDescription = it.description
                            editedTag = mapTagToLocalizedString(it.tag, context)
                            existingPhotoUrls = it.photoUrls
                            existingDocumentUrls = it.documentUrls
                            editedPhotoUris = emptyList()
                            editedDocumentUris = emptyList()
                            removedPhotoUrls = emptyList()
                            removedDocumentUrls = emptyList()
                            showEditDialog = true
                        },
                        onDelete = { showDeleteNoteDialog = it },
                        notesViewModel = notesViewModel
                    )
                }
            }
            Log.d("NotesScreen", "Displaying ${fetchedNotes.size} notes")
        }
    }

    if (showEditDialog && noteToEdit != null) {
        BasicAlertDialog(
            onDismissRequest = {
                showEditDialog = false
                noteToEdit = null
            },
            properties = DialogProperties(
            usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .width(350.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBG
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(  verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = stringResource(R.string.edit_note),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        DropdownSelector(
                            selectedValue = editedTag,
                            options = tags.filter { it != stringResource(R.string.all) },
                            onValueChange = { editedTag = it },
                            modifier = Modifier
                                .width(150.dp)
                                .padding(start=40.dp)
                                .offset(y =-7.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text(stringResource(R.string.description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (existingPhotoUrls.isNotEmpty() || editedPhotoUris.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.photos),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(existingPhotoUrls.filter {url ->
                                !removedPhotoUrls.contains(url)
                            }) {
                                photoUrl ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(photoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Pet photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = ColorPainter(Color(0xFFE0E0E0))
                                    )
                                    IconButton(
                                        onClick = {
                                            removedPhotoUrls = removedPhotoUrls + photoUrl
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.White, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove Photo",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            items(editedPhotoUris) { uri ->
                                Box(modifier = Modifier.size(100.dp)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "New photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        placeholder = ColorPainter(PrimaryColor)
                                    )
                                    IconButton(
                                        onClick = {
                                            editedPhotoUris = editedPhotoUris.filter { it != uri }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.White, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove Photo",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (existingDocumentUrls.isNotEmpty() || editedDocumentUris.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.documents),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            // Show existing documents that haven't been marked for removal
                            existingDocumentUrls.filter { url ->
                                !removedDocumentUrls.contains(url)
                            }.forEach { documentUrl ->
                                val fileName = documentUrl.substringAfterLast("/")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = "Document",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = fileName,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            removedDocumentUrls = removedDocumentUrls + documentUrl
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove Document",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            editedDocumentUris.forEach { uri ->
                                val context = LocalContext.current
                                val fileName = uri.lastPathSegment ?: stringResource(R.string.document)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = "New Document",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = fileName,
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            editedDocumentUris = editedDocumentUris.filter { it != uri }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove Document",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Photo picker launcher
                        val photoPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetMultipleContents()
                        ) { uris: List<Uri> ->
                            editedPhotoUris = editedPhotoUris + uris
                        }

                        // Document picker launcher
                        val documentPickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetMultipleContents()
                        ) { uris: List<Uri> ->
                            editedDocumentUris = editedDocumentUris + uris
                        }

                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = stringResource(R.string.add_photos2), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { documentPickerLauncher.launch("*/*") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryColor,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = stringResource(R.string.add_documents2), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(0.9.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    noteToEdit?.let { note ->
                                        val newPhotoUrls = mutableListOf<String>()
                                        val newDocumentUrls = mutableListOf<String>()
                                        if(editedPhotoUris.isNotEmpty()) {
                                            try {
                                                newPhotoUrls.addAll(
                                                    notesViewModel.uploadPhotos(editedPhotoUris)
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(context,
                                                    context.getString(
                                                        R.string.failed_to_upload_photos,
                                                        e.message
                                                    ), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        if (editedDocumentUris.isNotEmpty()) {
                                            try {
                                                newDocumentUrls.addAll(
                                                    notesViewModel.uploadDocuments(editedDocumentUris)
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(context,
                                                    context.getString(
                                                        R.string.failed_to_upload_documents,
                                                        e.message
                                                    ), Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        val updatedPhotoUrls = existingPhotoUrls.filter {
                                            !removedPhotoUrls.contains(it)
                                        } + newPhotoUrls

                                        val updatedDocumentUrls = existingDocumentUrls.filter {
                                            !removedDocumentUrls.contains(it)
                                        } + newDocumentUrls

                                        val updatedNote = note.copy(
                                            description = editedDescription,
                                            tag = reverseMapLocalizedTagToStorageFormat(editedTag, context),
                                            photoUrls = updatedPhotoUrls,
                                            documentUrls = updatedDocumentUrls
                                        )
                                        notesViewModel.updateNote(
                                            updatedNote,
                                            onSuccess = {
                                                Toast.makeText(context,
                                                    context.getString(R.string.note_updated_successfully), Toast.LENGTH_SHORT).show()
                                                refreshTrigger++
                                                showEditDialog = false
                                            },
                                            onFailure = { error ->
                                                Toast.makeText(context,
                                                    context.getString(
                                                        R.string.failed_to_update_note,
                                                        error
                                                    ), Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = stringResource(R.string.save), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showDeleteNoteDialog = noteToEdit
                                showEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightRed,
                                contentColor = DarkRed
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.delete), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showEditDialog = false
                                noteToEdit = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightGrey,
                                contentColor = Color.Black,
                            )
                        ) {
                            Text(text = stringResource(R.string.cancel), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteNoteDialog != null) {
        BasicAlertDialog(
            onDismissRequest = { showDeleteNoteDialog = null }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBG
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.warning),
                        color = DarkRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.note_deletion_alert),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    showDeleteNoteDialog?.let { note ->
                                        notesViewModel.deleteNote(
                                            note,
                                            onSuccess = {
                                                Toast.makeText(context,
                                                    context.getString(R.string.note_deleted_successfully), Toast.LENGTH_SHORT).show()
                                                refreshTrigger++
                                                showDeleteNoteDialog = null
                                            },
                                            onFailure = { error ->
                                                Toast.makeText(context,
                                                    context.getString(
                                                        R.string.failed_to_delete_note,
                                                        error
                                                    ), Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkRed
                            )
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                        Button(
                            onClick = { showDeleteNoteDialog = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightGrey,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                    containerColor = CardBG
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp), // Ensure proper padding inside the Card
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.add_a_new_tag), fontSize =18.sp,fontWeight=FontWeight.Normal )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newTag,
                            onValueChange = { newTag = it },
                            placeholder = { Text(stringResource(R.string.enter_tag_name)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    if (newTag.text.isNotBlank()) {
                                        val updatedTags = tags.toMutableList().apply { add(newTag.text.trim()) }
                                        tags = updatedTags // Update the local state

                                        selectedPet?.id?.let { petId ->
                                            coroutineScope.launch {
                                                petTagsViewModel.updatePetTags(petId, updatedTags,
                                                    onSuccess = {
                                                        newTag = TextFieldValue("")
                                                        showDialog = false
                                                    },
                                                    onFailure = { e ->
                                                        Toast.makeText(context,
                                                            context.getString(
                                                                R.string.failed_to_add_tag,
                                                                e.message ?: "Unknown error"
                                                            ), Toast.LENGTH_SHORT).show()
                                                        tags = tags.filter { it != newTag.text.trim() }
                                                    })
                                            }
                                        }
                                    }
                                    showDialog = false
                                },
                                modifier = Modifier.padding(vertical = 8.dp).width(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(stringResource(R.string.add))
                            }

                            Button(
                                onClick = { showDialog = false },
                                modifier = Modifier.padding(vertical = 8.dp).width(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryColor,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelector(selectedValue: String, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val days = (1..31).map { it.toString() }

    Box {
        Card(
            modifier = Modifier
                .width(70.dp)
                .height(45.dp)
                .clickable { expanded = true }
                .padding(2.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            colors = CardDefaults.cardColors(
                containerColor = NoteBG,
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = selectedValue, modifier = Modifier.weight(1f), color = Color.Black)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NoteBG)

        ) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text(day) },
                    onClick = {
                        onValueChange(day)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownSelector(modifier: Modifier = Modifier, selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .clickable { expanded = true }
                .height(45.dp)
                .padding(2.dp),
            colors = CardDefaults.cardColors(
                containerColor = NoteBG,
            )

//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = selectedValue, modifier = Modifier.weight(1f), color = Color.Black)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NoteBG)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Notes,
    notesViewModel: NotesViewModel,
    onEdit: (Notes) -> Unit = {},
    onDelete: (Notes) -> Unit = {}
) {
    val context = LocalContext.current

    fun mapTagToLocalizedString(tag: String, context: Context): String {
        // Map English tags to resource IDs
        val tagResourceMap = mapOf(
            "all" to R.string.all,
            "vomit" to R.string.vomit,
            "stool" to R.string.stool,
            "cough" to R.string.cough,
            "vet" to R.string.vet,
            "water intake" to R.string.water_intake,
            "emotion" to R.string.emotion
            // Add all other tags you use
        )

        // Try to find the tag in lowercase form (for case insensitivity)
        val resourceId = tagResourceMap[tag.lowercase()]

        // If we found a matching resource ID, use it; otherwise use the original tag
        return if (resourceId != null) {
            context.getString(resourceId)
        } else {
            tag // Return original for custom tags that don't have translations
        }
    }

    val localizedTag = notesViewModel.mapStorageFormatToDisplayTag(note.tag, context)

    fun getTagColor(tag: String, context: Context): Color {
        return when (tag) {
            context.getString(R.string.all) -> All
            context.getString(R.string.vomit) -> Vomit
            context.getString(R.string.stool) -> Stool
            context.getString(R.string.cough) -> Cough
            context.getString(R.string.vet) -> Vet
            context.getString(R.string.water_intake) -> WaterIntake
            context.getString(R.string.emotion) -> Emotion
            else -> Else
        }
    }

    Card(
        modifier = Modifier
            .width(400.dp)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBG
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical=12.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.getFormattedDate(),
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight= FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(0.8f))

                    Text(
                        text = localizedTag,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = getTagColor(localizedTag, context),
                        modifier = Modifier
                            .background( getTagColor(localizedTag, context).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.weight(0.1f))


                    IconButton(onClick = { onEdit(note) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = Color.Gray
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGrey),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        note.description,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color=(Color.Black)
                    )
                }

                // Display photos if available
                if (note.photoUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.photos2),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(Color(0xFFE0E0E0))
                            )
                        }
                    }
                }

                // Display document names if available
                if (note.documentUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.documents2),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
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
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fileName,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    textDecoration = TextDecoration.Underline,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

