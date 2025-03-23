package fi.oamk.petnotes.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fi.oamk.petnotes.R
import fi.oamk.petnotes.model.Pet
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
                    Text(text = "Home Screen") // You can customize the title as needed
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isUserLoggedIn) {
                if (pets.isNotEmpty()) {
                    // Display pets in cards
                    pets.forEach { pet ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${pet.name} - ${pet.breed}",
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Age: ${pet.age} years",
                                    style = TextStyle(fontWeight = FontWeight.Normal)
                                )
                            }
                        }
                    }
                } else {
                    // Show "No pets available" if the user has no pets
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Add your first pet to start",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                navController.navigate("addNewPet") // Navigate to AddNewPetScreen
                            }) {
                                Text("Add Pet")
                            }
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
