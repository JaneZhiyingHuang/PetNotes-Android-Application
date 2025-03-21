package fi.oamk.petnotes.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
    // Observing the current user from the ViewModel dynamically using collectAsState
    val user = googleSignInViewModel.auth.currentUser

    // Redirect to home if user is authenticated
    if (user != null) {
        // Navigate to the Home screen as soon as the user is authenticated
        LaunchedEffect(user) {
            Log.d("LandingScreen", "User is signed in: ${user.displayName}")
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
        Spacer(modifier = Modifier.height(50.dp))

        // Google Sign-In Button
        Button(onClick = { googleSignInViewModel.startGoogleSignIn() },
            modifier = Modifier
                .width(280.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD9D9D9),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email Sign-In Button
        Button(onClick = { navController.navigate("sign_in") },
            modifier = Modifier
                .width(280.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD9D9D9),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Continue with Email",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("or")
        Spacer(modifier = Modifier.height(8.dp))

        // Sign Up Button
        Text(text = "Don't have an account?", color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("sign_up") },
            modifier = Modifier
                .width(280.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD9D9D9),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Sign up with a new account",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}