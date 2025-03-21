package fi.oamk.petnotes.viewmodel

import android.app.Application
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import fi.oamk.petnotes.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoogleSignInViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize Firebase Auth first
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(application)

    // Use StateFlow instead of LiveData for Compose compatibility
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    // Track sign-in state
    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn

    // Track sign-in errors
    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError

    // Initiates the Google sign-in process using Google Identity Services
    fun startGoogleSignIn() {
        _isSigningIn.value = true
        _signInError.value = null

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)  // Show all accounts, including new ones
            .setAutoSelectEnabled(false)           // Don't auto-select accounts, show picker
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    getApplication<Application>().applicationContext,
                    request
                )

                if (result.credential != null) {
                    handleSignIn(result.credential)
                } else {
                    Log.w(TAG, "No credential received")
                    _signInError.value = "Sign-in was canceled"
                    _isSigningIn.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign-in: ${e.localizedMessage}", e)
                _signInError.value = "Sign-in failed: ${e.localizedMessage}"
                _isSigningIn.value = false
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing Google credential: ${e.localizedMessage}", e)
                _signInError.value = "Failed to process credential"
                _isSigningIn.value = false
            }
        } else {
            Log.w(TAG, "Credential is not a Google ID token")
            _signInError.value = "Invalid credential type"
            _isSigningIn.value = false
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "Firebase authentication successful")
                    updateUI(auth.currentUser)
                } else {
                    Log.w("TAG", "Firebase authentication failed", task.exception)
                    _signInError.value = task.exception?.localizedMessage ?: "Authentication failed"
                    updateUI(null)
                }
                _isSigningIn.value = false
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        _user.value = user
    }

    fun signOut() {
        auth.signOut() // Sign out of Firebase
        _user.value = null // Clear the user state
        Log.d("TAG", "User signed out successfully")
    }

    companion object {
        private const val TAG = "GoogleSignInViewModel"
    }
}
