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
import fi.oamk.petnotes.viewmodel.SignInViewModel
import fi.oamk.petnotes.viewmodel.SignUpViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PetNotesTheme {
                val navController = rememberNavController()
                val googleSignInViewModel: GoogleSignInViewModel = viewModel() // Move viewModel() inside the composable scope

                NavHost(navController = navController, startDestination = "landing") {
                    composable("landing") {
                        LandingScreen(navController = navController, googleSignInViewModel = googleSignInViewModel)
                    }
                    composable("sign_in") {
                        val signInViewModel: SignInViewModel = viewModel() // Move viewModel() inside the composable scope
                        SignInScreen(viewModel = signInViewModel, navController = navController)
                    }
                    composable("sign_up") {
                        val signUpViewModel: SignUpViewModel = viewModel() // Move viewModel() inside the composable scope
                        SignUpScreen(navController = navController, viewModel = signUpViewModel)
                    }
                    composable("home") {
                        // HomeScreen should also receive the view model if needed
                        HomeScreen(navController, googleSignInViewModel = googleSignInViewModel, )
                    }
                }
            }
        }
    }
}
