package fi.oamk.petnotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel

@Composable
fun HomeScreen(
    // Add the navController parameter
    homeScreenViewModel: HomeScreenViewModel, // ViewModel for handling sign-out and login check
    onSignOut: () -> Unit // Add onSignOut parameter
) {
    // Get the current user's login state using the ViewModel
    val isUserLoggedIn = homeScreenViewModel.isUserLoggedIn()

    // Recompose when the user logs out
    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            onSignOut() // Trigger sign-out action when user is null
        }
    }

    if (isUserLoggedIn) {
        // Show user's email and sign-out button
        val user = FirebaseAuth.getInstance().currentUser
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome, ${user?.email}",
                        style = TextStyle(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        homeScreenViewModel.signOut() // Sign out user using ViewModel
                        onSignOut() // Handle navigation after sign-out
                    }) {
                        Text(text = "Sign Out")
                    }
                }
            }
        }
    } else {
        // If no user is logged in, show a different message
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No user is currently logged in",
                color = Color.Red
            )
        }
    }
}
