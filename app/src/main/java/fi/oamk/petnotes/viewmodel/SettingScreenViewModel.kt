package fi.oamk.petnotes.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import fi.oamk.petnotes.utils.LanguageManager
import kotlinx.coroutines.tasks.await

class SettingScreenViewModel : ViewModel() {

    // Sign out the current user
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    // Check if the user is currently logged in
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    suspend fun deleteAccountAndAllData(): Pair<Boolean, String?> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return Pair(false, "No user is currently logged in")

        return try {
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()

            // Step 1: Fetch all pets
            val petsSnapshot = db.collection("users")
                .document(userId)
                .collection("pets")
                .get()
                .await()

            for (petDoc in petsSnapshot.documents) {
                val petId = petDoc.id


                // Delete pet image from Firebase Storage
                val petImageUri = petDoc.getString("petImageUri")
                if (!petImageUri.isNullOrEmpty()) {
                    try {
                        // Extract file path from the URL and remove the gs:// prefix
                        val filePath = petImageUri.substringAfter("gs://group3-petnotes.firebasestorage.app/").replace("%2F", "/")

                        // Check if filePath is valid
                        if (filePath.isNotEmpty()) {
                            Log.d("DeleteAccount", "Attempting to delete file at path: $filePath")

                            // Get reference to the file in Firebase Storage
                            val storageRef = storage.getReference(filePath)

                            // Try to delete the file
                            storageRef.delete().await()
                            Log.d("DeleteAccount", "Successfully deleted image at: $filePath")
                        } else {
                            Log.w("DeleteAccount", "Failed to extract file path from URL: $petImageUri")
                        }
                    } catch (e: Exception) {
                        Log.w("DeleteAccount", "Failed to delete image: $petImageUri", e)
                    }
                }




                // 2. Delete ALL FILES under this pet's folder in Firebase Storage
                try {
                    val petFolderRef = storage.getReference(petId)  // petId is the folder name

                    // List and delete all files in the folder
                    val files = petFolderRef.listAll().await()
                    for (file in files.items) {
                        try {
                            file.delete().await()
                            Log.d("DeleteAccount", "Successfully deleted file: ${file.name}")
                        } catch (e: Exception) {
                            Log.e("DeleteAccount", "Failed to delete file: ${file.name}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DeleteAccount", "Error listing or deleting files for pet $petId", e)
                }

                // 3. Delete pet_weights
                val weightSnapshots = db.collection("pet_weights")
                    .whereEqualTo("petId", petId)
                    .get()
                    .await()

                for (weightDoc in weightSnapshots.documents) {
                    weightDoc.reference.delete().await()
                }

                // 4. Delete notes
                val noteSnapshots = db.collection("notes")
                    .whereEqualTo("petId", petId)
                    .get()
                    .await()

                for (noteDoc in noteSnapshots.documents) {
                    noteDoc.reference.delete().await()
                }

                // 5. Delete pet document
                petDoc.reference.delete().await()
            }

            // Step 2: Delete user document
            db.collection("users").document(userId).delete().await()

            // Step 3: Delete Firebase user account
            currentUser.delete().await()

            Pair(true, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DeleteAccount", "Error deleting account and data", e)
            Pair(false, e.message ?: "Unknown error occurred")
        }
    }






    fun changeLanguage(context: Context, languageCode: String) {
        LanguageManager.setLocale(context, languageCode)
    }

    fun getCurrentLanguage(context: Context): String {
        return LanguageManager.getLanguageCode(context)
    }

}
