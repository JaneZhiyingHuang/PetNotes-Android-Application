package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AddNewPetViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // add new pet
    suspend fun addPet(
        petName: String,
        petGender: String,
        petSpecie: String,
        petDateOfBirth: String,
        petBreed: String,
        petMedicalCondition: String,
        petMicrochipNumber: String,
        petInsuranceCompany: String,
        petInsuranceNumber: String,
        petImageUri: String,
        onSuccess: () -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val pet = hashMapOf(
                "name" to petName,
                "gender" to petGender,
                "specie" to petSpecie,
                "dateOfBirth" to petDateOfBirth,
                "breed" to petBreed,
                "medicalCondition" to petMedicalCondition,
                "microchipNumber" to petMicrochipNumber,
                "insuranceCompany" to petInsuranceCompany,
                "insuranceNumber" to petInsuranceNumber,
                "petImageUri" to petImageUri
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
                Log.e("AddNewPet", "Error adding pet", e)
            }
        }
    }

    // update pet
    suspend fun updatePet(
        petId: String,
        petName: String,
        petGender: String,
        petSpecie: String,
        petDateOfBirth: String,
        petBreed: String,
        petMedicalCondition: String,
        petMicrochipNumber: String,
        petInsuranceCompany: String,
        petInsuranceNumber: String,
        petImageUri: String,
        onSuccess: () -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val petUpdates = hashMapOf(
                "name" to petName,
                "gender" to petGender,
                "specie" to petSpecie,
                "dateOfBirth" to petDateOfBirth,
                "breed" to petBreed,
                "medicalCondition" to petMedicalCondition,
                "microchipNumber" to petMicrochipNumber,
                "insuranceCompany" to petInsuranceCompany,
                "insuranceNumber" to petInsuranceNumber,
                "petImageUri" to petImageUri
            )
            try {
                db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId)
                    .update(petUpdates as Map<String, Any>)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AddNewPet", "Error updating pet", e)
            }
        }
    }
}
