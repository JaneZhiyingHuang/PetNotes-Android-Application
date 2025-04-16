package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fi.oamk.petnotes.model.Notes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class PetTagsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Backing field for tag counts
    private val _tagCounts = mutableStateListOf<TagCount>()
    val tagCounts: List<TagCount> = _tagCounts

    // Function to fetch pet tags from Firestore
    suspend fun fetchPetTags(
        petId: String,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                // Fetch the pet document from Firestore
                val petDocument = db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId)
                    .get()
                    .await()

                // Retrieve the tags from the document (default to an empty list if not present)
                val tags = petDocument.get("tags") as? List<String> ?: emptyList()

                // Pass the tags to the onSuccess callback
                onSuccess(tags)
            } catch (e: Exception) {
                // Handle failure and pass the exception to onFailure callback
                onFailure(e)
            }
        } else {
            // Handle the case where the user is not logged in
            onFailure(Exception("User is not logged in"))
        }
    }
    // Function to update tags field in the pet document (if needed)
    suspend fun updatePetTags(
        petId: String,
        tags: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                db.collection("users")
                    .document(userId)
                    .collection("pets")
                    .document(petId)
                    .update("tags", tags)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
    fun fetchTagCountsFromNotes(petId: String, visibleMonth: YearMonth) {
        Log.d("PetTagsViewModel", "Fetching tag counts for petId: $petId in visibleMonth: $visibleMonth")

        db.collection("notes")
            .whereEqualTo("petId", petId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("PetTagsViewModel", "Fetched ${querySnapshot.size()} notes for petId: $petId")

                val tagFrequencyMap = mutableMapOf<String, Int>()
                val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

                for (document in querySnapshot) {
                    Log.d("PetTagsViewModel", "Document data: ${document.data}")
                    val tag = document.getString("tag")
                    val dateString = document.getString("date")

                    if (!tag.isNullOrEmpty() && !dateString.isNullOrEmpty()) {
                        try {
                            val noteDate = LocalDate.parse(dateString, formatter)
                            val noteMonth = YearMonth.from(noteDate)

                            if (noteMonth == visibleMonth) {
                                tagFrequencyMap[tag] = tagFrequencyMap.getOrDefault(tag, 0) + 1
                            } else {
                                Log.d("PetTagsViewModel", "Skipping note not in $visibleMonth: $dateString")
                            }
                        } catch (e: Exception) {
                            Log.e("PetTagsViewModel", "Error parsing date: $dateString", e)
                        }
                    } else {
                        Log.w("PetTagsViewModel", "Invalid tag or date in note ${document.id}")
                    }
                }

                // Update the tag counts
                _tagCounts.clear()
                if (tagFrequencyMap.isNotEmpty()) {
                    _tagCounts.addAll(tagFrequencyMap.map { TagCount(it.key, it.value) })
                }

                Log.d("PetTagsViewModel", "Final tag counts for $visibleMonth: $_tagCounts")
            }
            .addOnFailureListener { exception ->
                Log.e("PetTagsViewModel", "Error fetching tags for petId: $petId", exception)
            }
    }


    private val _tagDateInfoList = MutableStateFlow<List<TagDateInfo>>(emptyList())
    val tagDateInfoList: StateFlow<List<TagDateInfo>> = _tagDateInfoList

    fun fetchTagCountsAndDatesFromNotes(petId: String) {
        Log.d("PetTagsViewModel", "Fetching tag counts and dates for petId: $petId")

        db.collection("notes")
            .whereEqualTo("petId", petId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("PetTagsViewModel", "Fetched ${querySnapshot.size()} notes for petId: $petId")

                val tagDataMap = mutableMapOf<String, MutableList<String>>() // tag -> list of dates

                for (document in querySnapshot) {
                    Log.d("PetTagsViewModel", "Document data: ${document.data}")

                    val tag = document.getString("tag")
                    val dateString = document.getString("date") ?: "Unknown date"


                    Log.d("PetTagsViewModel", "Tag: $tag | Date: $dateString")

                    if (!tag.isNullOrEmpty()) {
                        tagDataMap.getOrPut(tag) { mutableListOf() }.add(dateString)
                    } else {
                        Log.w("PetTagsViewModel", "Missing or empty tag in document ${document.id}")
                    }
                }

                val newList = tagDataMap.map { (tag, dates) ->
                    TagDateInfo(tag = tag, count = dates.size, dates = dates)
                }

                _tagDateInfoList.value = newList // Updating the StateFlow with the new list

                Log.d("PetTagsViewModel", "Final tagDateInfoList: $_tagDateInfoList")
            }
            .addOnFailureListener { exception ->
                Log.e("PetTagsViewModel", "Error fetching tag data for petId: $petId", exception)
            }
    }

    data class TagDateInfo(
        val tag: String,
        val count: Int,
        val dates: List<String>
    )


    data class TagCount(val tag: String, val count: Int)


    private val _selectedNotes = mutableStateOf<List<Notes>>(emptyList())
    val selectedNotes: State<List<Notes>> = _selectedNotes

    // Function to fetch notes for the selected date and petId
    fun fetchNotesForSelectedDay(petId: String, selectedDay: LocalDate) {
        viewModelScope.launch {
            val notes = getNotesByPetIdAndDate(petId, selectedDay)
            _selectedNotes.value = notes
        }
    }

    suspend fun getNotesByPetIdAndDate(petId: String, date: LocalDate): List<Notes> {
        // Format date to match the stored format in Firestore
        val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-M-d"))

        return try {
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("notes")
                .whereEqualTo("petId", petId)
                .whereEqualTo("date", formattedDate) // Use formatted date
                .orderBy("userSelectedTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("NotesViewModel", "Fetched ${querySnapshot.size()} notes for pet $petId on date $formattedDate")
            querySnapshot.toObjects(Notes::class.java)
        } catch (e: Exception) {
            Log.e("NotesViewModel", "Error fetching notes", e)
            emptyList() // Return empty list in case of error
        }
    }




}