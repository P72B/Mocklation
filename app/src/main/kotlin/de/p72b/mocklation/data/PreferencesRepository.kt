package de.p72b.mocklation.data

import android.content.Context
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


private const val PREFS_FILE_NAME = "preference"
private const val KEY_SELECTED_FEATURE = "selected_feature"
private const val KEY_SHOULD_ASK_AGAIN_TO_SHOW_STOP_SIMULATION = "should_ask_again_stop_running_simulation"

class PreferencesRepository(
    private val context: Context
) {
    private val _featureSelectedState = MutableStateFlow(
        getSelectedFeature().let {
            SelectedIdState.Status(it)
        }
    )
    val featureSelectedState: StateFlow<SelectedIdState> = _featureSelectedState

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
        _featureSelectedState.value = SelectedIdState.Status(id)
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

    fun getShouldAskAgainToStopSimulationService(): Boolean {
        val sharedPreference = context.getSharedPreferences(
            PREFS_FILE_NAME,
            ComponentActivity.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(
            KEY_SHOULD_ASK_AGAIN_TO_SHOW_STOP_SIMULATION,
            true
        )
    }

    fun setShouldAskAgainToStopSimulationService(value: Boolean) {
        val sharedPreference = context.getSharedPreferences(
            PREFS_FILE_NAME,
            ComponentActivity.MODE_PRIVATE
        )
        sharedPreference.edit().putBoolean(
            KEY_SHOULD_ASK_AGAIN_TO_SHOW_STOP_SIMULATION,
            value
        ).apply()
    }

    sealed interface SelectedIdState {
        data class Status(val id: String?) : SelectedIdState
    }
}