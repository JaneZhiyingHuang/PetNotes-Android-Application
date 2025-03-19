package fi.oamk.petnotes.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import fi.oamk.petnotes.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoogleSignInViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(application)
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    // Initiates the Google sign-in process
    fun startGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)  // Ensure that you aren't filtering for existing users
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                // Attempt to retrieve saved credentials
                val result = credentialManager.getCredential(
                    getApplication<Application>().applicationContext,
                    request
                )

                // Correctly handle the Google ID token credential
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                handleSignIn(googleIdTokenCredential)

            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error retrieving credentials: ${e.localizedMessage}")
                initiateManualGoogleSignIn() // Fallback to manual sign-in if retrieval fails
            }
        }
    }

    // Starts the manual Google Sign-In flow to allow the user to select a Google account
    private fun initiateManualGoogleSignIn() {
        // Launch the Google Sign-In intent to allow the user to pick a Google account
        val signInIntent = Intent(Intent.ACTION_VIEW)
        signInIntent.putExtra(
            Intent.EXTRA_EMAIL,
            "user@example.com"
        ) // Fake example, replace with real logic
        getApplication<Application>().startActivity(signInIntent)
    }

    // Handles the sign-in result once a credential is retrieved
    private fun handleSignIn(credential: GoogleIdTokenCredential) {
        val idToken = credential.idToken
        if (idToken.isNotEmpty()) {
            firebaseAuthWithGoogle(idToken)
        } else {
            Log.w("GoogleSignIn", "Empty ID token received")
        }
    }

    // Authenticates the user with Firebase using the retrieved Google ID Token
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
        Log.d("GoogleSignIn", "User signed out successfully")
    }
}