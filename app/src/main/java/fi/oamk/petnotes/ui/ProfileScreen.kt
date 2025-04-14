package fi.oamk.petnotes.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.model.PetDataStore
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedPet != null) {
                // card for pet image
                PetImageCard(pet = selectedPet!!)
                // card for pet infos
                PetInfoCard(pet = selectedPet!!, navController = navController) // 传入 navController 用于导航
            } else {
                Text(text = "Pet not found")
            }
        }
    }
}

@Composable
fun PetImageCard(pet: Pet){
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(top = 16.dp)
            .width(352.dp)
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
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .width(352.dp)
            .offset(y = (-30).dp)
    ) {
        // Wrap in a scrollable column to avoid content being cut off
        Column(modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {

            // title (Pet Profile)
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pet Profile",
                    style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
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
            InfoRow(label = "Pet Name:", text = pet.name)
            InfoRow(label = "Gender:", text = pet.gender)
            InfoRow(label = "Specie:", text = pet.specie)
            InfoRow(label = "Date of Birth:", text = pet.dateOfBirth)
            InfoRow(label = "Age:", text = calculateAge(pet.dateOfBirth))
            InfoRow(label = "Breed:", text = pet.breed)
            InfoRow(label = "Medical Condition:", text = pet.medicalCondition)
            InfoRow(label = "Microchip Number:", text = pet.microchipNumber)
            InfoRow(label = "Insurance Company:", text = pet.insuranceCompany)
            InfoRow(label = "Insurance Number:", text = pet.insuranceNumber)

            Spacer(modifier = Modifier.height(30.dp))

            // Delete button
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "*Delete profile of this pet.",
                    style = TextStyle(fontSize = 16.sp, color = Color.DarkGray),
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

//for info card style
@Composable
fun InfoRow(label: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 40.dp)) {
        Text(
            text = label,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
        )
        Spacer(modifier =Modifier.height(1.dp))
        Text(
            text = text,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        )
    }
}
