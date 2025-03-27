package fi.oamk.petnotes.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Pet(
    @DocumentId val id: String = "",
    val name: String = "",
    val gender: String = "",
    val specie: String = "",
    val breed: String = "",
    @PropertyName("dateOfBirth") val dateOfBirth: String = "",
    val age: Int = 0,
    val medicalCondition: String = "",
    val microchipNumber: String = "",
    val insuranceCompany: String = "",
    val insuranceNumber: String = "",
    val tags: List<String> = emptyList()
)
