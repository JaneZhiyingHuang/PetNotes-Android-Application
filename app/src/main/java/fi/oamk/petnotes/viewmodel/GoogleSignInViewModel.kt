package fi.oamk.petnotes.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.lifecycle.MutableLiveData
import fi.oamk.petnotes.R

class GoogleSignInViewModel(application: Application) : AndroidViewModel(application) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var credentialManager: CredentialManager = CredentialManager.create(application)
    private val googleSignInClient: GoogleSignInClient

    init {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getApplication<Application>().getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(getApplication(), googleSignInOptions)
    }

    // Initiates the Google sign-in process
    fun startGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(getApplication<Application>().applicationContext, request)
                if (result.credential != null) {
                    // Handle sign-in if credential is found
                    handleSignIn(result.credential)
                } else {
                    Log.d(TAG, "No credentials available, starting the manual sign-in flow.")
                    initiateGoogleSignIn()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving credentials: ${e.localizedMessage}")
                initiateGoogleSignIn() // Fallback to manual sign-in if Credential Manager fails
            }
        }
    }

    // Handles manual Google Sign-In when no saved credentials are found
    private fun initiateGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        getApplication<Application>().startActivity(signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    updateUI(auth.currentUser)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Update UI based on authentication state
        if (user != null) {
            Log.d(TAG, "User signed in: ${user.displayName}")
        } else {
            Log.d(TAG, "User signed out.")
        }
    }

    companion object {
        private const val TAG = "GoogleSignInViewModel"
    }
}
