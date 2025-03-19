package fi.oamk.petnotes.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _isButtonEnabled = MutableLiveData(true)
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isSignedIn = MutableLiveData<FirebaseUser?>()
    val isSignedIn: LiveData<FirebaseUser?> get() = _isSignedIn

    fun signInWithEmailAndPassword(email: String, password: String) {
        _isButtonEnabled.value = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    Log.d(TAG, "signInWithEmail:success")
                    _isSignedIn.value = auth.currentUser
                } else {
                    // Sign-in failure
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    _errorMessage.value = task.exception?.message ?: "Sign-in failed"
                }

                _isButtonEnabled.value = true
            }
    }

    companion object {
        private const val TAG = "SignInViewModel"
    }
}
