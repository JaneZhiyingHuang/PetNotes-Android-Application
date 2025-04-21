package fi.oamk.petnotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.model.PetDataStore
import fi.oamk.petnotes.ui.theme.CardBG
import fi.oamk.petnotes.ui.theme.DarkRed
import fi.oamk.petnotes.ui.theme.LineColor
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.viewmodel.AddNewPetViewModel
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    homeScreenViewModel: HomeScreenViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val isUserLoggedIn = homeScreenViewModel.isUserLoggedIn()
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(context) {
        PetDataStore.getSelectedPetId(context).collect { storedPetId ->
            if (isUserLoggedIn) {
                val fetchedPets = homeScreenViewModel.fetchPets()
                pets = fetchedPets
                selectedPet = fetchedPets.find { it.id == storedPetId } ?: fetchedPets.firstOrNull()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { padding ->

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedPet != null) {
                // card for pet image
                PetImageCard(pet = selectedPet!!)
                // card for pet infos
                PetInfoCard(pet = selectedPet!!, navController = navController)
            } else {
                Text(text = stringResource(R.string.pet_not_found))
            }
        }
    }
}

@Composable
fun PetImageCard(pet: Pet){
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        modifier = Modifier
            .padding(top = 16.dp)
            .width(400.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(pet.petImageUri),
            contentDescription = "Pet Avatar",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)),
        )
    }
}

@Composable
fun PetInfoCard(pet: Pet, navController: NavController) {

    val addnewPetViewModel: AddNewPetViewModel = viewModel()
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(400.dp)
            .offset(y = (-30).dp)
    ) {
        // Wrap in a scrollable column to avoid content being cut off
        Column(modifier = Modifier
            .padding(16.dp)
        )
            {

            // title (Pet Profile)
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.pet_profile),
                    style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                        .padding(start = 50.dp),
                    textAlign = TextAlign.Center
                )

                // Edit icon on the right
                IconButton(
                    onClick = {
                        // pass petId to AddNewPetScreen for edit
                        navController.navigate("addNewPet?petId=${pet.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Pet Profile",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details ( Name,Gender...)
            InfoRow(label = stringResource(R.string.pet_name2), text = pet.name)
            GenderInfoRow(label = stringResource(R.string.gender2), genderValue = pet.gender)
            InfoRow(label = stringResource(R.string.specie2), text = pet.specie)
            InfoRow(label = stringResource(R.string.date_of_birth2), text = pet.dateOfBirth)
            InfoRow(label = stringResource(R.string.age), text = calculateAge(pet.dateOfBirth))
            InfoRow(label = stringResource(R.string.breed2), text = pet.breed)
            InfoRow(label = stringResource(R.string.medical_condition2), text = pet.medicalCondition)
            InfoRow(label = stringResource(R.string.microchip_number), text = pet.microchipNumber)
            InfoRow(label = stringResource(R.string.insurance_company2), text = pet.insuranceCompany)
            InfoRow(label = stringResource(R.string.insurance_number2), text = pet.insuranceNumber)

            Spacer(modifier = Modifier.height(30.dp))

            // Delete button
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.delete_profile_of_this_pet),
                        style = TextStyle(fontSize = 16.sp, color = DarkRed, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                            .clickable { showDialog = true },
                        textAlign = TextAlign.Center
                    )
                }

                if (showDialog) {

                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(stringResource(R.string.warning2)) },
                        text = { Text(
                            stringResource(R.string.are_you_sure_to_delete_profile_of_this_pet) +
                                    stringResource(R.string.this_will_delete_all_data_related_to_this_pet)
                        ) },


                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                coroutineScope.launch {
                                    addnewPetViewModel.deletePetAndRelatedData(petId = pet.id) {
                                        navController.popBackStack()
                                    }
                                }
                            }) {
                                Text(stringResource(R.string.delete2))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(stringResource(R.string.cancel2))
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }


//for info card style
@Composable
fun InfoRow(label: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 40.dp)) {
        Text(
            text = label,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Gray)
        )
        Spacer(modifier =Modifier.height(1.dp))
        Text(
            text = text,
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black)
        )
    }
}

@Composable
fun GenderInfoRow(label: String, genderValue: String) {
    val context = LocalContext.current
    val displayText = when(genderValue.lowercase()) {
        "male" -> context.getString(R.string.male)
        "female" -> context.getString(R.string.female)
        "other" -> context.getString(R.string.other)
        else -> {
            when {
                genderValue.equals(context.getString(R.string.male), ignoreCase = true) ||
                        genderValue.lowercase().contains("male") ->
                    context.getString(R.string.male)

                genderValue.equals(context.getString(R.string.female), ignoreCase = true) ||
                        genderValue.lowercase().contains("female") ->
                    context.getString(R.string.female)

                else -> genderValue
            }
        }
    }

    InfoRow(label = label, text = displayText)
}