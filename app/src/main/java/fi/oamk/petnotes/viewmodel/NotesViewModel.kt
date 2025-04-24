package fi.oamk.petnotes.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import fi.oamk.petnotes.R
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
import java.net.URLDecoder
import java.util.Locale
import java.util.UUID

class NotesViewModel : ViewModel() {
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()
    private val _notes = MutableStateFlow<List<Notes>>(emptyList())
    val notes: StateFlow<List<Notes>> = _notes.asStateFlow()

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val message: String) : UploadState()
        data class Error(val message: String, val exception: Exception? = null) : UploadState()
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

    suspend fun uploadPhotos(photoUris: List<Uri>): List<String> {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val photoUrls = mutableListOf<String>()

        photoUris.forEach { uri ->
            val photoFileName = "photo_${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val photoRef = storageRef.child("users/$userId/notes/photos/$photoFileName")

            try {
                val uploadTask = photoRef.putFile(uri).await()
                val downloadUrl = photoRef.downloadUrl.await().toString()
                photoUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to upload photo: ${e.message}")
                throw e
            }
        }

        return photoUrls
    }

    suspend fun uploadDocuments(documentUris: List<Uri>): List<String> {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val documentUrls = mutableListOf<String>()

        documentUris.forEach { uri ->
            val documentFileName = "document_${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val documentRef = storageRef.child("users/$userId/notes/documents/$documentFileName")

            try {
                val uploadTask = documentRef.putFile(uri).await()
                val downloadUrl = documentRef.downloadUrl.await().toString()
                documentUrls.add(downloadUrl)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to upload document: ${e.message}")
                throw e
            }
        }

        return documentUrls
    }

    fun updateNote(
        updatedNote: Notes,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Get reference to the notes collection
            val notesRef = Firebase.firestore.collection("notes")

            // Update the note document
            notesRef.document(updatedNote.id)
                .set(updatedNote)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun deleteNote(
        note: Notes,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Get reference to the notes collection
            val notesRef = Firebase.firestore.collection("notes")

            // Delete the note document
            notesRef.document(note.id)
                .delete()
                .addOnSuccessListener {
                    // If the note has photos or documents, delete them from storage too
                    if (note.photoUrls.isNotEmpty() || note.documentUrls.isNotEmpty()) {
                        deleteFilesFromStorage(note.photoUrls + note.documentUrls)
                    }
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun deleteFilesFromStorage(fileUrls: List<String>) {
        val storage = Firebase.storage

        fileUrls.forEach { fileUrl ->
            try {
                // Extract the path from the URL
                val path = fileUrl.substringAfter("firebase.com/o/").substringBefore("?")
                val decodedPath = URLDecoder.decode(path, "UTF-8")

                // Get reference to the file and delete it
                val fileRef = storage.reference.child(decodedPath)
                fileRef.delete().addOnFailureListener { e ->
                    Log.e("NotesViewModel", "Failed to delete file: $fileUrl", e)
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error parsing or deleting file: $fileUrl", e)
            }
        }
    }

    fun mapStorageFormatToDisplayTag(storageTag: String, context: android.content.Context): String {
        val displayTagMap = mapOf(
            "all" to context.getString(R.string.all),
            "vomit" to context.getString(R.string.vomit),
            "stool" to context.getString(R.string.stool),
            "cough" to context.getString(R.string.cough),
            "vet" to context.getString(R.string.vet),
            "water_intake" to context.getString(R.string.water_intake),
            "emotion" to context.getString(R.string.emotion)
        )

        return displayTagMap[storageTag] ?: storageTag.split("_")
            .joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } }
    }

    private fun reverseMapLocalizedTagToStorageFormat(localizedTag: String, context: android.content.Context): String {
        val reverseTagMap = mapOf(
            context.getString(fi.oamk.petnotes.R.string.all) to "all",
            context.getString(fi.oamk.petnotes.R.string.vomit) to "vomit",
            context.getString(fi.oamk.petnotes.R.string.stool) to "stool",
            context.getString(fi.oamk.petnotes.R.string.cough) to "cough",
            context.getString(fi.oamk.petnotes.R.string.vet) to "vet",
            context.getString(fi.oamk.petnotes.R.string.water_intake) to "water_intake",
            context.getString(fi.oamk.petnotes.R.string.emotion) to "emotion"
        )

        return reverseTagMap[localizedTag] ?: localizedTag.lowercase().replace(" ", "_")
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
        userSelectedTimestamp: Long,
        context: android.content.Context
    ) = viewModelScope.launch {
        if (selectedPet == null || userInput.isBlank()) {
            _uploadState.value = UploadState.Error("Pet or description is missing!")
            return@launch
        }

        _uploadState.value = UploadState.Loading

        val petId = selectedPet.id
        val tagKey = reverseMapLocalizedTagToStorageFormat(selectedTag, context)

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
                tag = tagKey,
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
