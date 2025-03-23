package fi.oamk.petnotes.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Pet(
    @DocumentId val id: String = "", // Firestore document ID
    val name: String = "",
    val breed: String = "",
    val age: Int = 0 // Age should be an integer
)
