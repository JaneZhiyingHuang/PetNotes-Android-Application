package fi.oamk.petnotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fi.oamk.petnotes.ui.theme.PetNotesTheme
import fi.oamk.petnotes.viewmodel.SignInViewModel
import fi.oamk.petnotes.viewmodel.SignUpViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetNotesTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "sign_in") {
                    composable("sign_in") {
                        // Use viewModel() to get an instance of SignInViewModel
                        val signInViewModel: SignInViewModel = viewModel()
                        SignInScreen(viewModel = signInViewModel, navController = navController)
                    }
                    composable("sign_up") {
                        // Use viewModel() to get an instance of SignUpViewModel
                        val signUpViewModel: SignUpViewModel = viewModel()
                        SignUpScreen(navController = navController, viewModel = signUpViewModel)
                    }
                    composable("home") {
                        // HomeScreen should also receive the view model if needed
                        HomeScreen()
                    }
                }
            }
        }
    }
}
