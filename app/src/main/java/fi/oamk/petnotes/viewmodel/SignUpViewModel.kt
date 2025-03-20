package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUp(
        email: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
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

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUpViewModel", "createUserWithEmail:success")
                        onResult(true, null)
                    } else {
                        Log.w("SignUpViewModel", "createUserWithEmail:failure", task.exception)
                        onResult(false, task.exception?.localizedMessage ?: "Sign-up failed")
                    }
                }
        }
    }
}