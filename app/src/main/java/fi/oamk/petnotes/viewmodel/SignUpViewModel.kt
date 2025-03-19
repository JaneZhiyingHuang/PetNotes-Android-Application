package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    // Function to perform sign-up logic (replace this with actual sign-up logic)
    fun signUp(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit // Removed @Composable annotation
    ) {
        if (username.isBlank()) {
            onResult(false, "Username is required")
            return
        }
        if (email.isBlank()) {
            onResult(false, "Email is required")
            return
        }
        if (password.isBlank()) {
            onResult(false, "Password is required")
            return
        }
        if (confirmPassword.isBlank()) {
            onResult(false, "Please confirm your password")
            return
        }
        if (password != confirmPassword) {
            onResult(false, "Passwords do not match")
            return
        }

        // Simulate sign-up logic (e.g., calling an API)
        viewModelScope.launch {
            try {
                // Simulating a sign-up process (replace with actual sign-up logic)
                onResult(true, null) // Indicate success
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sign-up failed")
            }
        }
    }
}
