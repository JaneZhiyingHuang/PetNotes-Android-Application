package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val user = hashMapOf(
                            "email" to email,
                            "userId" to userId,
                            "password" to password,
                            "createdAt" to FieldValue.serverTimestamp(),
                        )

                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d("SignUpViewModel", "User data stored successfully")
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.w("SignUpViewModel", "Error storing user data", e)
                                onResult(false, "Failed to store user data")
                            }
                    } else {
                        Log.w("SignUpViewModel", "createUserWithEmail:failure", task.exception)
                        onResult(false, task.exception?.localizedMessage ?: "Sign-up failed")
                    }
                }
        }
    }
}
