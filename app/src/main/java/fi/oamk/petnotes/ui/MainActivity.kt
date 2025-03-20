package fi.oamk.petnotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fi.oamk.petnotes.ui.theme.PetNotesTheme
import fi.oamk.petnotes.viewmodel.GoogleSignInViewModel
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel
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
                val homeScreenViewModel: HomeScreenViewModel = viewModel() // Initialize HomeScreenViewModel here

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
                    composable("home") {
                        HomeScreen(
                            homeScreenViewModel = homeScreenViewModel, // Pass the ViewModel to HomeScreen
                            onSignOut = {
                                // Sign-out and navigate to the landing screen
                                navController.navigate("landing") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true // To avoid adding "home" to back stack again
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
