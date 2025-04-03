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
                .collection("pets") // Pets collection
                .get()
                .addOnSuccessListener { snapshot ->
                    // Delete all pets in this user's pets collection
                    for (document in snapshot.documents) {
                        val petDocRef = document.reference

                        // Deleting pet weights if exists
                        petDocRef.collection("pet_weights").get()
                            .addOnSuccessListener { weightSnapshot ->
                                for (weightDoc in weightSnapshot.documents) {
                                    weightDoc.reference.delete() // Delete individual weight records
                                }

                                // Now delete the pet document itself
                                petDocRef.delete()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Successfully deleted pet and pet weights
                                        } else {
                                            onComplete(false, task.exception?.message ?: "Error deleting pet")
                                            return@addOnCompleteListener
                                        }
                                    }
                            }
                            .addOnFailureListener { exception ->
                                onComplete(false, exception.message ?: "Error fetching pet weights")
                                return@addOnFailureListener
                            }
                    }

                    // Now, delete the user's document from 'users' collection
                    db.collection("users").document(userId).delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Now proceed to delete the Firebase user
                                currentUser.delete()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onComplete(true, null) // Successfully deleted account
                                        } else {
                                            onComplete(false, task.exception?.message) // Error deleting Firebase user
                                        }
                                    }
                            } else {
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
