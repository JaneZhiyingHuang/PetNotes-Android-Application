package fi.oamk.petnotes.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class HomeScreenViewModel : ViewModel() {

    // Check if the user is currently logged in
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }}

