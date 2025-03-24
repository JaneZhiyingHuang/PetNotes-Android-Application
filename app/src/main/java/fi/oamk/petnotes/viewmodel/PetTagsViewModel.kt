package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PetTagsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Function to update tags for a specific pet
    suspend fun updatePetTags(petId: String, tags: List<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId)
                    .update("tags", tags)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}