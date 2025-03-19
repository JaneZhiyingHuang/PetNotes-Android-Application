package fi.oamk.petnotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fi.oamk.petnotes.viewmodel.GoogleSignInViewModel

@Composable
fun HomeScreen(navController: NavController, googleSignInViewModel: GoogleSignInViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding to prevent content from touching the edges
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centers items vertically in the Column
    ) {
        // Text to display "Home Screen" with appropriate style and color
        Text(
            text = "Home Screen",
            modifier = Modifier.padding(bottom = 16.dp), // Add padding below text for spacing
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black // Ensure text color is visible
        )

        // Button to allow the user to sign out
        Button(
            onClick = {
                googleSignInViewModel.signOut()
                navController.navigate("landing") {
                    popUpTo("landing") { inclusive = true }  // Ensures we clear the back stack
                }            }
        ) {
            Text("Sign Out")
        }
    }
}

