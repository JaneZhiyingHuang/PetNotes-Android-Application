package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeightViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())

    private val _petName = MutableStateFlow<String?>(null)
    val petName: StateFlow<String?> = _petName

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    private val _weightEntries = MutableStateFlow<List<Pair<Date, Float>>>(emptyList())
    val weightEntries: StateFlow<List<Pair<Date, Float>>> = _weightEntries

    // Function to load pet data and weight entries from Firestore
    fun loadPetData(petId: String, userId: String) {
        viewModelScope.launch {
            try {
                // Fetch pet data
                val petDocument = db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId)
                    .get()
                    .await()

                _petName.value = petDocument.getString("name") ?: "Unknown Pet"

                // Fetch weight data
                val weightDocuments = db.collection("pet_weights")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("petId", petId)
                    .get()
                    .await()

                val parsedWeights = weightDocuments.documents.mapNotNull { doc ->
                    val dateString = doc.getString("date") ?: return@mapNotNull null
                    val date = try {
                        dateFormat.parse(dateString)
                    } catch (e: Exception) {
                        Log.e("WeightViewModel", "Error parsing date: $dateString", e)
                        null
                    }

                    val weight = when (val weightValue = doc.get("weight")) {
                        is Number -> weightValue.toFloat()
                        is String -> weightValue.toFloatOrNull()
                        else -> null
                    }

                    if (date != null && weight != null) {
                        Pair(date, weight)
                    } else {
                        null
                    }
                }.sortedBy { it.first }

                // Update state with parsed weights
                _weightEntries.value = parsedWeights

            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error fetching weight data", e)
                _snackbarMessage.value = "Error fetching weight data"
            }
        }
    }

    // Function to add a weight entry
    fun addWeight(petId: String, userId: String, weightValue: Float, selectedDate: Date?) {
        val dateToSave = selectedDate ?: Date() // Use selected date or current date if not selected
        val currentDate = dateFormat.format(dateToSave)

        val weightData = mapOf(
            "userId" to userId,
            "petId" to petId,
            "date" to currentDate,
            "weight" to weightValue
        )

        viewModelScope.launch {
            try {
                // Add weight data to Firestore
                db.collection("pet_weights").add(weightData).await()
                Log.d("WeightViewModel", "Added weight entry: $weightData")

                // Add the new entry to the local list and sort
                val newEntry = dateToSave to weightValue
                val updatedWeightEntries = _weightEntries.value.toMutableList().apply {
                    add(newEntry)
                    sortBy { it.first }
                }

                _weightEntries.value = updatedWeightEntries

                _snackbarMessage.value = "Weight added successfully!"
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error adding weight", e)
                _snackbarMessage.value = "Failed to add weight"
            }
        }
    }

    // Function to delete a weight entry
    fun deleteWeightEntry(petId: String, userId: String, date: Date) {
        viewModelScope.launch {
            try {
                // Find and delete the entry from Firebase
                val weightDocument = db.collection("pet_weights")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("petId", petId)
                    .whereEqualTo("date", dateFormat.format(date))
                    .get()
                    .await()

                weightDocument.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Remove from the local list after deletion
                val updatedWeightEntries = _weightEntries.value.toMutableList().apply {
                    removeAll { it.first == date }
                    sortByDescending { it.first }
                }

                _weightEntries.value = updatedWeightEntries

                _snackbarMessage.value = "Weight entry deleted successfully!"
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error deleting weight entry", e)
                _snackbarMessage.value = "Failed to delete weight entry"
            }
        }
    }
}
