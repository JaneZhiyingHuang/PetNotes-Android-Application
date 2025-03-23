package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingScreenViewModel : ViewModel() {

    // Sign out the current user
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    // Check if the user is currently logged in
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun deleteAccount(onComplete: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            // Get user ID
            val userId = currentUser.uid
            val db: FirebaseFirestore = FirebaseFirestore.getInstance()

            // Deleting user's pet data from Firestore (if it exists)
            db.collection("users")
                .document(userId)
                .collection("pets") // If pets are stored in a subcollection called 'pets'
                .get()
                .addOnSuccessListener { snapshot ->
                    // Delete all pets in this user's pets collection
                    for (document in snapshot.documents) {
                        document.reference.delete() // Delete individual pet documents
                    }

                    // Now, delete the user's document from 'users' collection
                    db.collection("users").document(userId).delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Now proceed to delete the Firebase user
                                currentUser.delete()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onComplete(true, null) // Successfully deleted
                                        } else {
                                            onComplete(false, task.exception?.message) // Error in deleting user
                                        }
                                    }
                            } else {
                                // If Firestore data deletion fails
                                onComplete(false, task.exception?.message ?: "Error deleting Firestore data")
                            }
                        }
                }
                .addOnFailureListener { exception ->
                    // Handle error when fetching the user's pet data
                    onComplete(false, exception.message ?: "Error fetching user's pets")
                }
        } ?: onComplete(false, "No user is currently logged in")
    }
}
