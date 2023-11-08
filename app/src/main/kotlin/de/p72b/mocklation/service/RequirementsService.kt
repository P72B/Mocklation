package de.p72b.mocklation.service

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.p72b.mocklation.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class RequirementsService(private val application: Context) : LifecycleEventObserver {

    private val _requirementsState = MutableStateFlow<RequirementsState>(RequirementsState.Status())
    val requirementsState: StateFlow<RequirementsState> = _requirementsState
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> runChecks()
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START,
            Lifecycle.Event.ON_PAUSE,
            Lifecycle.Event.ON_STOP,
            Lifecycle.Event.ON_DESTROY,
            Lifecycle.Event.ON_ANY -> {
                // Nothing to do here
            }
        }
    }

    private fun runChecks() {
        val areDeveloperOptionsEnabledOnPhone = Settings.Secure.getInt(
            application.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
        _requirementsState.value = RequirementsState.Status(
            isDeveloperOptionsEnabled = areDeveloperOptionsEnabledOnPhone
        )
        val opsManager: AppOpsManager =
            application.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val isMockLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                opsManager.unsafeCheckOp(
                    AppOpsManager.OPSTR_MOCK_LOCATION,
                    Process.myUid(),
                    BuildConfig.APPLICATION_ID
                ) == AppOpsManager.MODE_ALLOWED
            } catch (e: SecurityException) {
                false
            }
        } else {
            !Settings.Secure.getString(application.contentResolver, "mock_location").equals("0")
        }
        _requirementsState.value = RequirementsState.Status(
            isDeveloperOptionsEnabled = areDeveloperOptionsEnabledOnPhone,
            isSelectedMockLocationApp = isMockLocation
        )


        if (areDeveloperOptionsEnabledOnPhone && isMockLocation) {

            _requirementsState.value = RequirementsState.Ready
        }
    }
}

sealed interface RequirementsState {
    data class Status(
        val isDeveloperOptionsEnabled: Boolean = false,
        val isSelectedMockLocationApp: Boolean = false
    ) : RequirementsState

    data object Ready : RequirementsState
}