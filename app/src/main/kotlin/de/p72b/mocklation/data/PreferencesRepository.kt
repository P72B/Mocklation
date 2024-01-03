package de.p72b.mocklation.data

import android.content.Context
import androidx.activity.ComponentActivity


private const val PREFS_FILE_NAME = "preference"
private const val KEY_SELECTED_FEATURE = "selected_feature"

class PreferencesRepository(
    private val context: Context
) {

    fun firstTimeAskingPermission(permission: String, isFirstTime: Boolean) {
        val sharedPreference = context.getSharedPreferences(
            PREFS_FILE_NAME,
            ComponentActivity.MODE_PRIVATE
        )
        sharedPreference.edit().putBoolean(
            permission,
            isFirstTime
        ).apply()
    }

    fun isFirstTimeAskingPermission(permission: String): Boolean {
        val sharedPreference = context.getSharedPreferences(
            PREFS_FILE_NAME,
            ComponentActivity.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(
            permission,
            true
        )

    }

    fun setSelectedFeature(id: String?) {
        val sharedPreference = context.getSharedPreferences(
            PREFS_FILE_NAME,
            ComponentActivity.MODE_PRIVATE
        )
        sharedPreference.edit().putString(
            KEY_SELECTED_FEATURE,
            id
        ).apply()
    }

    fun getSelectedFeature(): String? {
        val sharedPreference = context.getSharedPreferences(
            PREFS_FILE_NAME,
            ComponentActivity.MODE_PRIVATE
        )
        return sharedPreference.getString(
            KEY_SELECTED_FEATURE,
            null
        )

    }
}