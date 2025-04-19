package fi.oamk.petnotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fi.oamk.petnotes.R
import fi.oamk.petnotes.ui.theme.PrimaryColor
import fi.oamk.petnotes.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.bgpurple),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                        .alpha(0.2f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top // Set vertical arrangement to top
                ) {
                    // Title: "Sign Up"
                    Spacer(modifier = Modifier.height(100.dp)) // Add space to push title down
                    Text(
                        text = "Sign Up!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp), // Move title further down
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    // Email Field
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start // Align label to the left
                    ) {
                        // Label for Email
                        Text(
                            text = "Email",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 73.dp) // Adjust left padding for label
                        )
                        // Centered OutlinedTextField
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .width(280.dp)
                                .padding(4.dp)
                                .height(56.dp)
                                .background(Color.White, shape = RoundedCornerShape(40.dp))
                                .align(Alignment.CenterHorizontally), // Center the text field
                            shape = RoundedCornerShape(40.dp) // Rounded corners for the text field
                        )
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    // Password Field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Label for Password
                        Text(
                            text = "Password",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 73.dp)
                        )
                        // Centered OutlinedTextField
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            visualTransformation = PasswordVisualTransformation(), // Mask the password text
                            modifier = Modifier
                                .width(280.dp)
                                .padding(4.dp)
                                .height(56.dp)
                                .background(Color.White, shape = RoundedCornerShape(40.dp))
                                .align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(40.dp) // Rounded corners for the text field
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirm Password Field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally // Center the whole content horizontally
                    ) {
                        // Label for Confirm Password
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start // Left-align the label
                        ) {
                            Text(
                                text = "Confirm Password",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 73.dp) // Adjust left padding for label
                            )
                        }

                        // Centered OutlinedTextField for Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            visualTransformation = PasswordVisualTransformation(), // Mask the password text
                            modifier = Modifier
                                .width(280.dp)
                                .padding(4.dp)
                                .height(56.dp)
                                .background(Color.White, shape = RoundedCornerShape(40.dp))
                                .align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(40.dp) // Rounded corners for the text field
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display error message if needed
                    if (errorMessage.isNotEmpty()) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Already have an account? Sign In" link
                    TextButton(
                        onClick = {
                            navController.navigate("sign_in") // Navigate to the Sign In screen
                        },
                        modifier = Modifier.fillMaxWidth() // Make the button fill the width
                    ) {
                        Text(
                            text = "Already have an account? Sign In",
                            color = Color.Gray // Change the text color to Gray
                        )
                    }

                    // Create Account Button
                    Button(
                        onClick = {
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }

                            isButtonEnabled = false // Disable button while signing up

                            viewModel.signUp(email, password, confirmPassword) { success, error ->
                                isButtonEnabled = true // Re-enable the button after attempt

                                if (success) {
                                    navController.navigate("sign_in") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = error ?: "Sign-up failed"
                                }
                            }
                        },
                        modifier = Modifier
                            .width(280.dp)
                            .align(Alignment.CenterHorizontally)
                            .height(56.dp), // Full width button
                        enabled = isButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White // Match the text color
                        )
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()
    SignUpScreen(navController = navController)
}