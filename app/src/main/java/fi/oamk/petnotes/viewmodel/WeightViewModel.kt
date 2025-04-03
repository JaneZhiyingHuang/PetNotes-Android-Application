//package fi.oamk.petnotes.viewmodel
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import java.text.SimpleDateFormat
//import java.util.*
//
//class WeightViewModel : ViewModel() {
//
//    private val db = FirebaseFirestore.getInstance()
//    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
//    private val displayDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault()) // For x-axis labels
//
//    var petName = mutableStateOf<String?>(null)
//    var weightEntries = mutableStateListOf<Pair<Date, Float>>()
//    var snackbarMessage = mutableStateOf<String?>(null)
//
//    fun loadPetData(petId: String, userId: String) {
//        viewModelScope.launch {
//            try {
//                val petDocument = db.collection("users")
//                    .document(userId)
//                    .collection("pets")
//                    .document(petId)
//                    .get()
//                    .await()
//
//                petName.value = petDocument.getString("name") ?: "Unknown Pet"
//
//                val weightDocuments = db.collection("pet_weights")
//                    .whereEqualTo("userId", userId)
//                    .whereEqualTo("petId", petId)
//                    .get()
//                    .await()
//
//                val parsedWeights = weightDocuments.documents.mapNotNull { doc ->
//                    val dateString = doc.getString("date") ?: return@mapNotNull null
//                    val date = try {
//                        dateFormat.parse(dateString)
//                    } catch (e: Exception) {
//                        Log.e("WeightViewModel", "Error parsing date: $dateString", e)
//                        null
//                    }
//
//                    val weight = when (val weightValue = doc.get("weight")) {
//                        is Number -> weightValue.toFloat()
//                        is String -> weightValue.toFloatOrNull()
//                        else -> null
//                    }
//
//                    if (date != null && weight != null) {
//                        Pair(date, weight)
//                    } else {
//                        null
//                    }
//                }.sortedBy { it.first }
//
//                weightEntries.clear()
//                weightEntries.addAll(parsedWeights)
//
//            } catch (e: Exception) {
//                Log.e("WeightViewModel", "Error fetching weight data", e)
//                snackbarMessage.value = "Error fetching weight data"
//            }
//        }
//    }
//
//    fun addWeight(petId: String, userId: String, weightValue: Float, selectedDate: Date?) {
//        val dateToSave = selectedDate ?: Date() // Use selected date or current date if not selected
//        val currentDate = dateFormat.format(dateToSave)
//
//        val weightData = mapOf(
//            "userId" to userId,
//            "petId" to petId,
//            "date" to currentDate,
//            "weight" to weightValue
//        )
//
//        viewModelScope.launch {
//            try {
//                db.collection("pet_weights").add(weightData).await()
//                Log.d("WeightViewModel", "Added weight entry: $weightData")
//
//                // Refresh weight list
//                weightEntries.add(dateToSave to weightValue)
//                weightEntries.sortBy { it.first }
//
//                snackbarMessage.value = "Weight added successfully!"
//            } catch (e: Exception) {
//                Log.e("WeightViewModel", "Error adding weight", e)
//                snackbarMessage.value = "Failed to add weight"
//            }
//        }
//    }
//
//    fun deleteWeightEntry(petId: String, userId: String, date: Date) {
//        viewModelScope.launch {
//            try {
//                // Find and delete the entry from Firebase
//                val weightDocument = db.collection("pet_weights")
//                    .whereEqualTo("userId", userId)
//                    .whereEqualTo("petId", petId)
//                    .whereEqualTo("date", dateFormat.format(date))
//                    .get()
//                    .await()
//
//                weightDocument.documents.forEach { doc ->
//                    doc.reference.delete().await()
//                }
//
//                // Remove from the local list after deletion
//                weightEntries.removeAll { it.first == date }
//
//                // Sort by date in descending order (latest first)
//                weightEntries.sortByDescending { it.first }
//
//                snackbarMessage.value = "Weight entry deleted successfully!"
//            } catch (e: Exception) {
//                Log.e("WeightViewModel", "Error deleting weight entry", e)
//                snackbarMessage.value = "Failed to delete weight entry"
//            }
//        }
//    }
//}
