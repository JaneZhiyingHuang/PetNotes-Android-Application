package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            // Replace with actual authentication logic
            if (email == "test@petnotes.com" && password == "password") {
                onResult(true, null)
            } else {
                onResult(false, "Invalid credentials")
            }
        }
    }
}
