package fi.oamk.petnotes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import fi.oamk.petnotes.viewmodel.HomeScreenViewModel

@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel,
    onSignOut: () -> Unit
) {
    val isUserLoggedIn = homeScreenViewModel.isUserLoggedIn()
    val context = LocalContext.current

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            onSignOut()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isUserLoggedIn) {
            val user = remember { FirebaseAuth.getInstance().currentUser }
            val email = user?.email ?: "Unknown Email"
            val username = email.substringBefore("@") // Extracts the username part

            Column(
                modifier = Modifier
                    .weight(1f)
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
                            text = "Username: $username",
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "Email: $email",
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            homeScreenViewModel.signOut()
                            onSignOut()
                        }) {
                            Text(text = "Sign Out")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                homeScreenViewModel.deleteAccount { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                        onSignOut()
                                    } else {
                                        Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(text = "Delete Account")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
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

        BottomNavigationBar()
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(containerColor = Color(0xFFEFEFEF)) {
        val items = listOf("Home", "Notes", "Map", "Settings")
        items.forEach { item ->
            NavigationBarItem(
                selected = false, // Handle selection logic as needed
                onClick = { /* Handle navigation logic here */ },
                icon = { /* You can add icons for each item here */ },
                label = { Text(text = item) }
            )
        }
    }
}
