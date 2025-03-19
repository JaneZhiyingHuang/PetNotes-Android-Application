package fi.oamk.petnotes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fi.oamk.petnotes.viewmodel.GoogleSignInViewModel

@Composable
fun LandingScreen(navController: NavController, googleSignInViewModel: GoogleSignInViewModel) {
    val userState = googleSignInViewModel.user.collectAsState()
    val user = userState.value

    // Redirect to home if user is authenticated
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate("home") {
                popUpTo("landing") { inclusive = true } // Clears back stack
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to PetNotes!", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Let's login to continue")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            googleSignInViewModel.startGoogleSignIn()
        }) {
            Text("Continue with Google")
        }

        Button(onClick = { navController.navigate("sign_in") }) {
            Text("Continue with Email")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("or")
        Text(text = "Don't have an account?", color = Color.LightGray)
        Button(onClick = { navController.navigate("sign_up") }) {
            Text("Sign up with a new account")
        }
    }
}