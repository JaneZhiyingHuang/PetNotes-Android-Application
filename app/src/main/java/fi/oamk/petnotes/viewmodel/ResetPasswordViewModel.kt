package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Email cannot be empty")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Reset email sent successfully")
                    onResult(true, null)
                } else {
                    Log.w(TAG, "Reset email failed", task.exception)
                    onResult(false, task.exception?.message ?: "Failed to send reset email")
                }
            }
    }

    companion object {
        private const val TAG = "ResetPasswordViewModel"
    }
}