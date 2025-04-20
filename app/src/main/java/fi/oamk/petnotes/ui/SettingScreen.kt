package fi.oamk.petnotes.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fi.oamk.petnotes.ui.theme.CardBG
import fi.oamk.petnotes.ui.theme.LightRed
import fi.oamk.petnotes.ui.theme.LineColor
import fi.oamk.petnotes.R
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.ui.theme.SecondaryColor
import fi.oamk.petnotes.utils.LanguageManager
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
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    val availableLanguages = listOf(
        "en" to "English",
        "fi" to "Finnish",
        "sv" to "Swedish",
        "zh" to "Chinese",
    )

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            onSignOut()
        }
    }


    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Setting",
                                color = SecondaryColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryColor
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = LineColor)
            }
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CardBG),
            ) {

            if (isUserLoggedIn) {
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email ?: stringResource(R.string.unknown_email)
                val username = email.substringBefore("@")

                Spacer(modifier = Modifier.height(100.dp))
                // Title: "User Info" in bold and larger font
                Text(
                    text = stringResource(R.string.user_info),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(start = 73.dp, bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.user_name),
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
                    text = stringResource(R.string.user_email),
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
                        containerColor = LightRed,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.delete_account),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change Language Button
                Button(
                    onClick = {
                        showLanguageDialog = true
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.change_language),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (showLanguageDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showLanguageDialog = false },
                        title = { Text("Select Language") },
                        text = {
                            LazyColumn {
                                items(availableLanguages) { (code, name) ->
                                    Text(
                                        text = name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                LanguageManager.applyLanguageAndRecreate(
                                                    context as Activity,
                                                    code
                                                )
                                                showLanguageDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showLanguageDialog = false }) {
                                Text("Cancel")
                            }
                        }
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
                        containerColor = PrimaryColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.join_membership),
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
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.sign_out),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Text(text = stringResource(R.string.no_user_is_currently_logged_in), color = Color.Red)
            }

        }
    }
}