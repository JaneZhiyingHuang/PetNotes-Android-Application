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

    suspend fun uploadFilesToFirebase(
        files: List<Uri>,
        fileType: String,
        petId: String
    ): List<String> = withContext(Dispatchers.IO) {
        if (files.isEmpty()) {
            return@withContext emptyList<String>()
        }

        _uploadState.value = UploadState.Loading
        val storage = FirebaseStorage.getInstance()
        val uploadedFileUrls = mutableListOf<String>()

        try {
            files.forEach { uri ->
                val fileName = "${fileType}_${UUID.randomUUID()}"
                val storageRef = storage.reference.child("$petId/$fileName")

                val downloadUrl = storageRef.putFile(uri).await().storage.downloadUrl.await()
                uploadedFileUrls.add(downloadUrl.toString())
            }

            _uploadState.value = UploadState.Success("Files uploaded successfully")
            return@withContext uploadedFileUrls
        } catch (e: Exception) {
            _uploadState.value = UploadState.Error("Upload failed: ${e.message}")
            return@withContext emptyList<String>()
        }
    }

    fun createNote(note: Notes) = viewModelScope.launch {
        _uploadState.value = UploadState.Loading

        try {
            FirebaseFirestore.getInstance().collection("notes")
                .add(note)
                .await()

            _uploadState.value = UploadState.Success("Note created successfully")
        } catch (e: Exception) {
            _uploadState.value = UploadState.Error("Note creation failed: ${e.message}")
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
        selectedTag: String
    ) = viewModelScope.launch {
        if (selectedPet == null || userInput.isBlank()) {
            _uploadState.value = UploadState.Error("Pet or description is missing!")
            return@launch
        }

        val petId = selectedPet.id
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
            documentUrls = documentUrls
        )

        createNote(note)
    }

    suspend fun getNotesByPetId(petId: String): List<Notes> {
        return try {
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("notes")
                .whereEqualTo("petId", petId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.toObjects(Notes::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
