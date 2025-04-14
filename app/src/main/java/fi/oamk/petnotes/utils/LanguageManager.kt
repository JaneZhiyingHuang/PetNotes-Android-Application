package fi.oamk.petnotes.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {
    private const val PREF_LANGUAGE = "language_prefs"
    private const val SELECTED_LANGUAGE = "selected_language"

    fun setLocale(context: Context, languageCode: String) {
        saveLanguagePreference(context, languageCode)

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageCode)
            )
        } else  {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)

            val config = Configuration(context.resources.configuration)
            config.setLocales(localeList)

            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            @Suppress("DEPRECATION")
            context.applicationContext.resources.updateConfiguration(
                config,
                context.applicationContext.resources.displayMetrics
            )
        }
    }

    fun getLanguageCode(context: Context): String {
        val pref = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        return pref.getString(SELECTED_LANGUAGE, Locale.getDefault().language) ?: "en"
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val pref = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        pref.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    fun applyLanguageAndRecreate(activity: android.app.Activity, languageCode: String) {
        setLocale(activity, languageCode)
        activity.recreate()
    }

    fun wrap(context: Context): ContextWrapper {
        val languageCode = getLanguageCode(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)

        return ContextWrapper(context.createConfigurationContext(config))
    }
}