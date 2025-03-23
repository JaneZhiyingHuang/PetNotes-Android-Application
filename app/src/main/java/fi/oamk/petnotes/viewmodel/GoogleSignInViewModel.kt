package fi.oamk.petnotes.viewmodel

import android.app.Application
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import kotlinx.coroutines.launch
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import fi.oamk.petnotes.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoogleSignInViewModel(application: Application) : AndroidViewModel(application) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var credentialManager: CredentialManager = CredentialManager.create(application)
    private val googleSignInClient: GoogleSignInClient

    // StateFlow to track authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // User state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getApplication<Application>().getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(getApplication(), googleSignInOptions)

        // Initialize the current user state
        _currentUser.value = auth.currentUser
        _authState.value = if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated

        // Add auth state listener to detect sign-in/sign-out events
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            _authState.value = if (firebaseAuth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated
            Log.d(TAG, "Auth state changed: ${firebaseAuth.currentUser?.displayName ?: "signed out"}")
        }
    }

    // Check if the device has an active internet connection
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // Initiates the Google sign-in process
    fun startGoogleSignIn() {
        // First check if network is available
        if (!isNetworkAvailable()) {
            _authState.value = AuthState.Error("No internet connection. Please check your network settings and try again.")
            return
        }

        _authState.value = AuthState.Loading

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getApplication<Application>().getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false) // Changed to false to avoid using cached credentials
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
                _authState.value = AuthState.Error("Failed to connect to Google. Please check your internet connection and try again.")
                // Don't automatically start manual sign-in on error - let the user retry
            }
        }
    }

    // Handles manual Google Sign-In when no saved credentials are found
    private fun initiateGoogleSignIn() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            getApplication<Application>().startActivity(signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Google Sign-In intent: ${e.localizedMessage}")
            _authState.value = AuthState.Error("Failed to launch Google Sign-In. Please try again later.")
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing Google credential: ${e.localizedMessage}")
                _authState.value = AuthState.Error("Failed to process Google credentials. Please try again.")
            }
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            _authState.value = AuthState.Error("Invalid credential type")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    _currentUser.value = auth.currentUser
                    _authState.value = AuthState.Authenticated
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    // Handle specific error cases
                    val errorMessage = when {
                        task.exception?.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Please check your internet connection and try again."
                        task.exception?.message?.contains("ERR_NAME_NOT_RESOLVED") == true ->
                            "DNS resolution error. Please check your internet connection and try again."
                        else -> task.exception?.localizedMessage ?: "Authentication failed. Please try again."
                    }

                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }

    // Retry authentication after error
    fun retry() {
        _authState.value = AuthState.Unauthenticated
    }

    sealed class AuthState {
        object Unauthenticated : AuthState()
        object Authenticated : AuthState()
        object Loading : AuthState()
        data class Error(val message: String) : AuthState()
    }

    companion object {
        private const val TAG = "GoogleSignInViewModel"
    }
}