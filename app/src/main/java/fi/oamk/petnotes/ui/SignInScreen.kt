package fi.oamk.petnotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import fi.oamk.petnotes.viewmodel.SignInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(navController: NavController, viewModel: SignInViewModel = viewModel()) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Observe ViewModel's LiveData
    val isButtonEnabled by viewModel.isButtonEnabled.observeAsState(true)
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val isSignedIn by viewModel.isSignedIn.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEFEFEF)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Text(
                text = "Sign In!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Centered form (email and password)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Email Field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Email",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 73.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .width(280.dp)
                            .padding(4.dp)
                            .height(56.dp)
                            .background(Color(0xFFD9D9D9), shape = RoundedCornerShape(40.dp))
                            .align(Alignment.CenterHorizontally),
                           shape = RoundedCornerShape(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Password Field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Password",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 73.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .width(280.dp)
                            .padding(4.dp)
                            .height(56.dp)
                            .background(Color(0xFFD9D9D9), shape = RoundedCornerShape(40.dp))
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Forget Password Button (Centered below the fields)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { /* Navigate to forget password screen */ },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(text = "Forget Password?", color = Color.Gray)
                    }
                }

                // Sign Up Link Above Button
                TextButton(
                    onClick = { navController.navigate("sign_up") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? Sign Up", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sign In Button
                Button(
                    onClick = {
                        viewModel.signInWithEmailAndPassword(email, password)
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp),
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD9D9D9),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Use let block to navigate when signed in
                LaunchedEffect(isSignedIn) {
                    if (isSignedIn != null) {
                        navController.navigate("home") {
                            popUpTo("sign_in") { inclusive = true }
                        }
                    }
                }

                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = Color.Red)
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewSignInScreen() {
    val navController = rememberNavController()
    SignInScreen(navController = navController)
}
