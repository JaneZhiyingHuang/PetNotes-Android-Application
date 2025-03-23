package fi.oamk.petnotes.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import fi.oamk.petnotes.viewmodel.GoogleSignInViewModel

@Composable
fun LandingScreen(navController: NavController, googleSignInViewModel: GoogleSignInViewModel) {
    // Observing the authentication state from the ViewModel
    val authState by googleSignInViewModel.authState.collectAsState()
    val currentUser by googleSignInViewModel.currentUser.collectAsState()

    // Navigate to home if authenticated
    LaunchedEffect(authState) {
        if (authState is GoogleSignInViewModel.AuthState.Authenticated && currentUser != null) {
            Log.d("LandingScreen", "User is authenticated: ${currentUser?.displayName}")
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

        // Show loading indicator during authentication
        when (authState) {
            is GoogleSignInViewModel.AuthState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Signing in...", style = MaterialTheme.typography.bodyMedium)
            }

            is GoogleSignInViewModel.AuthState.Error -> {
                // Display error message with retry button
                val errorMessage = (authState as GoogleSignInViewModel.AuthState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Retry button
                Button(
                    onClick = { googleSignInViewModel.retry() },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            else -> {
                // Google Sign-In Button
                Button(
                    onClick = { googleSignInViewModel.startGoogleSignIn() },
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
                Button(
                    onClick = { navController.navigate("sign_in") },
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
                Button(
                    onClick = { navController.navigate("sign_up") },
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
    }
}