package fi.oamk.petnotes.model

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pet_prefs")

object PetDataStore {
    private val SELECTED_PET_ID = stringPreferencesKey("selected_pet_id")

    fun getSelectedPetId(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[SELECTED_PET_ID]
        }
    }

    suspend fun setSelectedPetId(context: Context, petId: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_PET_ID] = petId
        }
    }
}
