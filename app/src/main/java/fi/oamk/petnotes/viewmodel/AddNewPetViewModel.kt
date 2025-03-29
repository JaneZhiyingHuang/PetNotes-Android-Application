package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AddNewPetViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

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
                "insuranceNumber" to petInsuranceNumber
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
