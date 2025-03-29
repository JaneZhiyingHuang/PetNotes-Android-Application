package fi.oamk.petnotes.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.Pet
import fi.oamk.petnotes.ui.theme.InputColor
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel,
    navController: NavController // Pass NavController to HomeScreen
) {
    val isUserLoggedIn = homeScreenViewModel.isUserLoggedIn()
    var pets by remember { mutableStateOf(listOf<Pet>()) }
    var selectedPetName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isUserLoggedIn) {
        Log.d("HomeScreen", "User logged in: $isUserLoggedIn") // Log user login status
        if (isUserLoggedIn) {
            coroutineScope.launch {
                pets = homeScreenViewModel.getPets() // Fetch pets from Firestore
                Log.d("HomeScreen", "Fetched pets: $pets") // Log pets data after it's fetched
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Home Screen")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("addNewPet") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_circle_outline_26),
                            contentDescription = "Add New Pet"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFEFEFEF))
            )



        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUserLoggedIn) {
                if (pets.isNotEmpty()) {
                    // Display pets in cards
                    pets.forEach { pet ->
                        Card(
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .width(352.dp)
//                                .height(214.dp)
                        ) {
                            Column(modifier = Modifier.padding(22.dp)) {
                                // First Row: Circle Image + Column with name, age, gender, dob
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(InputColor) // Placeholder color if no image
                                    ) {
//                                        Image(
//                                            painter = painterResource(id = R.drawable.ic_avatar),
//                                            contentDescription = "Avatar",
//                                            modifier = Modifier.fillMaxSize()
//                                        )
                                    }
                                    Spacer(modifier = Modifier.width(26.dp))
                                    Column {
                                        Text(
                                            text = pet.name, // Name
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp )
                                        )
                                        Text(
                                            text = "${pet.age} years old", // Age
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp)
                                        )
                                        Text(
                                            text = "${pet.gender}", // Gender
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp)                                        )
                                        Text(
                                            text = "${pet.dateOfBirth}", // DOB
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp)                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))

                                // Second Row: Medical Condition
                                Text(
                                    text = "*Medical Condition: ${pet.medicalCondition}",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                } else {
                    // Show "No pets available" if the user has no pets
                    Card(
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        modifier = Modifier
                            .padding(top = 150.dp)
                            .width(352.dp)
                            .height(214.dp),
                        onClick = {
                            navController.navigate("addNewPet") // Navigate to AddNewPetScreen
                        }
                    ){
                        Column(
                            modifier = Modifier.fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_circle_outline_26),
                                contentDescription = "Add New Pet"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Add your first pet to start !",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )


                        }
                    }
                }
            } else {
                // Show message if the user is not logged in
                Text(
                    text = "Please log in to continue.",
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}