package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fi.oamk.petnotes.model.Pet
import kotlinx.coroutines.tasks.await

class HomeScreenViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()  // Corrected firestore reference
    private val auth = FirebaseAuth.getInstance()

    // Check if the user is currently logged in
    fun isUserLoggedIn(): Boolean {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        Log.d("HomeScreenViewModel", "User logged in: $isLoggedIn") // Log login status
        return isLoggedIn
    }

    suspend fun getPets(): List<Pet> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val petList = mutableListOf<Pet>()

        if (userId != null) {
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("pets")
                    .get()
                    .await()

                for (document in snapshot) {
                    val pet = document.toObject(Pet::class.java)
                    petList.add(pet.copy())
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error fetching pets: ${e.message}")
            }
        }
        return petList
    }
}
