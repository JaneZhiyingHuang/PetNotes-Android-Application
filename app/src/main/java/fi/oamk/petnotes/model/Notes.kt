package fi.oamk.petnotes.model

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Notes (
    @DocumentId val id: String = "",
    val petId: String = "",
    val description: String = "",
    val date: String? = null,
    val tag: String = "",
    val photoUrls: List<String> = emptyList(),
    val documentUrls: List<String> = emptyList(),
) {
    fun getFormattedDate(): String {
        return date ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
}