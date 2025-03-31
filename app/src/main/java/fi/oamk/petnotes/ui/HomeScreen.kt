package fi.oamk.petnotes.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.ui.theme.InputColor
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
    navController: NavController
) {
    val isUserLoggedIn = homeScreenViewModel.isUserLoggedIn()
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isUserLoggedIn) {
        Log.d("HomeScreen", "User logged in: $isUserLoggedIn")
        if (isUserLoggedIn) {
            coroutineScope.launch {
                pets = homeScreenViewModel.fetchPets()
                if (pets.isNotEmpty()) {
                    selectedPet = pets.first()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("addNewPet") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_circle_outline_26),
                            contentDescription = "Add New Pet"
                        )
                    }
                },
                actions = {
                    if (pets.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedPet?.name ?: "Select Pet",
                                    fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                )
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Pet")
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                pets.forEach { pet ->
                                    DropdownMenuItem(
                                        text = { Text(pet.name) },
                                        onClick = {
                                            selectedPet = pet
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUserLoggedIn) {
                if (selectedPet != null) {
                    PetCard(selectedPet!!)
                } else {
                    NoPetsCard(navController)
                }
            } else {
                Text(
                    text = "Please log in to continue.",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun PetCard(pet: Pet) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .padding(top = 10.dp)
            .width(352.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(26.dp))
                Column {
                    Text(text = pet.name, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
                    Text(text = pet.gender, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    Text(text = pet.dateOfBirth, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    Text(text = calculateAge(pet.dateOfBirth), style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "*Medical Condition: ${pet.medicalCondition}", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NoPetsCard(navController: NavController) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.padding(top = 150.dp).width(352.dp).height(214.dp),
        onClick = { navController.navigate("addNewPet") }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painter = painterResource(id = R.drawable.baseline_add_circle_outline_26), contentDescription = "Add New Pet")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Add your first pet to start!", style = TextStyle(fontWeight = FontWeight.Bold))
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
