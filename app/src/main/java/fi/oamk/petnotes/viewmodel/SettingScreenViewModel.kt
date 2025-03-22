package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SettingScreenViewModel : ViewModel() {

    // Sign out the current user
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    // Check if the user is currently logged in
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    // Delete the current user's account
    fun deleteAccount(onComplete: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, null) // Successfully deleted
            } else {
                onComplete(false, task.exception?.message) // Error message
            }
        } ?: onComplete(false, "No user is currently logged in")
    }
}
