package fi.oamk.petnotes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fi.oamk.petnotes.viewmodel.SettingScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    settingScreenViewModel: SettingScreenViewModel,
    navController: NavController,
    onSignOut: () -> Unit
) {
    val isUserLoggedIn = remember { settingScreenViewModel.isUserLoggedIn() }
    val context = LocalContext.current

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            onSignOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEFEFEF)
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Center buttons horizontally
            verticalArrangement = Arrangement.Top
        ) {
            if (isUserLoggedIn) {
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email ?: "Unknown Email"
                val username = email.substringBefore("@")

                Spacer(modifier = Modifier.height(100.dp))
                // Title: "User Info" in bold and larger font
                Text(
                    text = "User Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(start = 73.dp, bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "User Name:",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(start = 73.dp, bottom = 5.dp)
                )
                Text(
                    text = username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(start = 73.dp, bottom = 5.dp)
                )
                Spacer(modifier = Modifier.height(8.dp)) // Space between Username and Email
                Text(
                    text = "User Email:",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(start = 73.dp, bottom = 5.dp)
                )
                Text(
                    text = email,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(start = 73.dp, bottom = 5.dp)
                )
                Spacer(modifier = Modifier.height(50.dp))
                // Delete Account Button
                Button(
                    onClick = {
                        settingScreenViewModel.deleteAccount { success, message ->
                            if (success) {
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                onSignOut()
                            } else {
                                Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD9D9D9),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Delete Account",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change Language Button
                Button(
                    onClick = {
                        // Action for Change Language button
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD9D9D9),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Change Language",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Join Membership Button
                Button(
                    onClick = {
                        // Action for Join Membership button
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD9D9D9),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Join Membership",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                // Sign Out Button
                Button(
                    onClick = {
                        settingScreenViewModel.signOut()
                        onSignOut()
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD9D9D9),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Text(text = "No user is currently logged in", color = Color.Red)
            }

        }
    }
}