package fi.oamk.petnotes.ui


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fi.oamk.petnotes.ui.theme.PetNotesTheme
import fi.oamk.petnotes.viewmodel.GoogleSignInViewModel
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
import fi.oamk.petnotes.viewmodel.NotesViewModel
import fi.oamk.petnotes.viewmodel.PetTagsViewModel
import fi.oamk.petnotes.viewmodel.SettingScreenViewModel
import fi.oamk.petnotes.viewmodel.SignInViewModel
import fi.oamk.petnotes.viewmodel.SignUpViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetNotesTheme {
                // Remember the NavController
                val navController = rememberNavController()

                // Initialize ViewModels
                val googleSignInViewModel: GoogleSignInViewModel = viewModel()
                val homeScreenViewModel: HomeScreenViewModel = viewModel()
                val settingScreenViewModel: SettingScreenViewModel = viewModel() // Initialize SettingScreenViewModel here
                val petTagsViewModel: PetTagsViewModel = viewModel()
                val notesViewModel: NotesViewModel = viewModel()

                // Set up NavHost
                NavHost(navController = navController, startDestination = "landing") {
                    composable("landing") {
                        LandingScreen(
                            navController = navController,
                            googleSignInViewModel = googleSignInViewModel
                        )
                    }
                    composable("sign_in") {
                        val signInViewModel: SignInViewModel = viewModel()
                        SignInScreen(viewModel = signInViewModel, navController = navController)
                    }
                    composable("sign_up") {
                        val signUpViewModel: SignUpViewModel = viewModel()
                        SignUpScreen(navController = navController, viewModel = signUpViewModel)
                    }
                    composable("reset_password") {
                        ResetPasswordScreen(navController = navController)
                    }
                    composable("home") {
                        HomeScreen(
                            homeScreenViewModel = homeScreenViewModel, // Pass the ViewModel to HomeScreen
                            navController = navController
                        )

                    }

                    composable("profile/{petId}") { backStackEntry ->
                        val petId = backStackEntry.arguments?.getString("petId") ?: ""
                        ProfileScreen(
                            petId = petId,
                            navController = navController,
                            homeScreenViewModel = homeScreenViewModel
                        )
                    }


                    composable("weight_screen/{userId}/{petId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        val petId = backStackEntry.arguments?.getString("petId")

                        if (userId != null && petId != null) {
                            WeightScreen(navController = navController, userId = userId, petId = petId)
                        } else {
                            Text(text = "User ID or Pet ID is missing.")
                        }
                    }

                    composable("map") {
                        // MapScreen doesn't need any ViewModel based on your code
                        val context = LocalContext.current // Get the context here
                        MapScreen(navController = navController, context = context)

                    }
                    composable("calendarScreen") {
                        val context = LocalContext.current
                        val homeScreenViewModel: HomeScreenViewModel = homeScreenViewModel

                        CalendarScreen(
                            navController = navController,
                            context = context,
                            homeScreenViewModel = homeScreenViewModel
                        )
                    }

                    composable("notes") {
                        NotesScreen(
                            navController = navController,
                            homeScreenViewModel = homeScreenViewModel,
                            petTagsViewModel = petTagsViewModel,
                            notesViewModel = notesViewModel
                        )
                    }
                    composable("addNewPet") {
                        AddNewPetScreen(
                            navController = navController
                        )
                    }

                    composable("setting") {
                        SettingScreen(
                            navController = navController,
                            settingScreenViewModel = settingScreenViewModel,
                            onSignOut = {
                                // Handle sign-out logic here
                                navController.navigate("landing") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
