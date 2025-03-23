package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AddNewPetViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    suspend fun addPet(petName: String, petBreed: String, petAge: Int, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val pet = hashMapOf(
                "name" to petName,
                "breed" to petBreed,
                "age" to petAge  // Store age as an Int
            )

            try {
                db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .add(pet)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
