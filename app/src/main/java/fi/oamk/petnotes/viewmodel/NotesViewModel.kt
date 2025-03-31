package fi.oamk.petnotes.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import fi.oamk.petnotes.model.Notes
import fi.oamk.petnotes.model.Pet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class NotesViewModel : ViewModel() {
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val message: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }

    private suspend fun uploadFilesToFirebase(
        files: List<Uri>,
        fileType: String,
        petId: String
    ): List<String> = withContext(Dispatchers.IO) {
        if (files.isEmpty()) {
            return@withContext emptyList<String>()
        }

        Log.d("NotesViewModel", "Starting upload of ${files.size} $fileType files")
        val storage = FirebaseStorage.getInstance()
        val uploadedFileUrls = mutableListOf<String>()

        try {
            files.forEach { uri ->
                Log.d("NotesViewModel", "Uploading $fileType with URI: $uri")
                val fileName = "${fileType}_${UUID.randomUUID()}"
                val storageRef = storage.reference.child("$petId/$fileName")

                val uploadTask = storageRef.putFile(uri).await()
                Log.d("NotesViewModel", "Upload completed, getting download URL")
                val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()
                Log.d("NotesViewModel", "Got download URL: $downloadUrl")
                uploadedFileUrls.add(downloadUrl.toString())
            }

            Log.d("NotesViewModel", "Successfully uploaded ${uploadedFileUrls.size} $fileType files")
            return@withContext uploadedFileUrls
        } catch (e: Exception) {
            Log.e("NotesViewModel", "Error uploading $fileType files", e)
            _uploadState.value = UploadState.Error("Upload failed: ${e.message}")
            return@withContext emptyList<String>()
        }
    }

    private fun createNote(note: Notes) = viewModelScope.launch {
//        _uploadState.value = UploadState.Loading

        try {
            FirebaseFirestore.getInstance().collection("notes")
                .add(note)
                .await()

            withContext(Dispatchers.Main) {
                _uploadState.value = UploadState.Success("Note created successfully")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _uploadState.value = UploadState.Error("Note creation failed: ${e.message}")
            }
            Log.e("NotesViewModel", "Upload error", e)
        }
    }

    // Function to handle both file uploads and note creation
    fun uploadAndCreateNote(
        photoUris: List<Uri>,
        documentUris: List<Uri>,
        selectedPet: Pet?,
        userInput: String,
        selectedYear: Int,
        selectedMonth: Int,
        selectedDate: Int,
        selectedTag: String,
        userSelectedTimestamp: Long
    ) = viewModelScope.launch {
        if (selectedPet == null || userInput.isBlank()) {
            _uploadState.value = UploadState.Error("Pet or description is missing!")
            return@launch
        }

        _uploadState.value = UploadState.Loading

        val petId = selectedPet.id

        try {
            val photoUrlsDeferred = async { uploadFilesToFirebase(photoUris, "photo", petId) }
            val documentUrlsDeferred = async { uploadFilesToFirebase(documentUris, "document", petId) }

            // Await the results of both uploads
            val photoUrls = photoUrlsDeferred.await()
            val documentUrls = documentUrlsDeferred.await()

            val note = Notes(
                petId = petId,
                description = userInput,
                date = "$selectedYear-$selectedMonth-$selectedDate",
                tag = selectedTag,
                photoUrls = photoUrls,
                documentUrls = documentUrls,
                userSelectedTimestamp = userSelectedTimestamp
            )

            createNote(note)
        } catch (e: Exception) {
            _uploadState.value = UploadState.Error("Upload failed: ${e.message}")
            Log.e("NotesViewModel","Upload error",e)

        }
    }

    suspend fun getNotesByPetId(petId: String): List<Notes> {
        return try {
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("notes")
                .whereEqualTo("petId", petId)
                .orderBy("userSelectedTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("NotesViewModel", "Fetched ${querySnapshot.size()} notes for pet $petId")
            querySnapshot.toObjects(Notes::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
