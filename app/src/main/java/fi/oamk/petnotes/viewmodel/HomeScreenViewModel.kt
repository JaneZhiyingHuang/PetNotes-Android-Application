package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import fi.oamk.petnotes.model.Pet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class HomeScreenViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()  // Firestore instance
    private val auth = FirebaseAuth.getInstance()  // Firebase authentication instance

    // StateFlow for list of pets
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    // StateFlow for selected pet
    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet

    // Check if the user is currently logged in
    fun isUserLoggedIn(): Boolean {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        Log.d("HomeScreenViewModel", "User logged in: $isLoggedIn") // Log login status
        return isLoggedIn
    }

    // Fetch pets from Firestore and update the pets list


    suspend fun fetchPets(): List<Pet> {
        val userId = auth.currentUser?.uid
        val petList = mutableListOf<Pet>()

        if (userId != null) {
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("pets")
                    .get()
                    .await()

                for (document in snapshot) {
                    var pet = document.toObject(Pet::class.java)

                    // Fetch image URL from Firebase Storage if needed
                    if (!pet.petImageUri.startsWith("http")) {
                        val imageRef = FirebaseStorage.getInstance().reference.child(pet.petImageUri)
                        pet = pet.copy(petImageUri = imageRef.downloadUrl.await().toString())
                    }

                    petList.add(pet)
                }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error fetching pets: ${e.message}")
            }
        }
        return petList
    }

    suspend fun fetchPetById(petId: String): Pet? {
        return try {
            val petDoc = FirebaseFirestore.getInstance()
                .collection("pets")
                .document(petId)
                .get()
                .await()

            if (petDoc.exists()) {
                petDoc.toObject(Pet::class.java)?.copy(id = petId)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("HomeScreenViewModel", "Error fetching pet by ID", e)
            null
        }
    }

    fun getUserId(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid ?: "" // Return the user ID or an empty string if no user is logged in
    }

}
