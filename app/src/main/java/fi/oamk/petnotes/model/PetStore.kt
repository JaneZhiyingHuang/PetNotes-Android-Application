package fi.oamk.petnotes.model

import com.google.android.gms.maps.model.LatLng

// Make sure the class is public so it can be accessed in other packages
data class PetStore(
    val name: String,
    val location: LatLng
)
