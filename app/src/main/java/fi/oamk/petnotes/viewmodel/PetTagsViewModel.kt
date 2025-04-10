package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class PetTagsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Backing field for tag counts
    private val _tagCounts = mutableStateListOf<TagCount>()
    val tagCounts: List<TagCount> = _tagCounts

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
    fun fetchTagCountsFromNotes(petId: String) {
        Log.d("PetTagsViewModel", "Fetching tag counts for petId: $petId")

        db.collection("notes")
            .whereEqualTo("petId", petId)  // Ensure filtering by petId
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("PetTagsViewModel", "Fetched ${querySnapshot.size()} notes for petId: $petId")

                val tagFrequencyMap = mutableMapOf<String, Int>()

                if (querySnapshot.isEmpty) {
                    Log.d("PetTagsViewModel", "No notes found for petId: $petId")
                }

                // Iterate through the fetched notes
                for (document in querySnapshot) {
                    Log.d("PetTagsViewModel", "Document data: ${document.data}")
                    val tag = document.getString("tag")  // Directly retrieve the "tag" string
                    Log.d("PetTagsViewModel", "Tag found in note ${document.id}: $tag")

                    if (!tag.isNullOrEmpty()) {
                        tagFrequencyMap[tag] = tagFrequencyMap.getOrDefault(tag, 0) + 1
                    } else {
                        Log.w("PetTagsViewModel", "Invalid or empty tag found in note ${document.id}")
                    }
                }

                // Log tag frequency map before updating _tagCounts
                Log.d("PetTagsViewModel", "Tag frequency map before updating: $tagFrequencyMap")

                // Update the state with the fetched tag counts
                _tagCounts.clear()
                if (tagFrequencyMap.isNotEmpty()) {
                    _tagCounts.addAll(tagFrequencyMap.map { TagCount(it.key, it.value) })
                } else {
                    Log.d("PetTagsViewModel", "No tags to update in _tagCounts")
                }

                // Log the final result
                Log.d("PetTagsViewModel", "Final tag counts: $_tagCounts")
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
}