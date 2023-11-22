package de.p72b.mocklation.ui.model.requirements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.service.RequirementsState
import de.p72b.mocklation.ui.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RequirementsViewModel(
    private val navigator: Navigator,
    private val requirementsService: RequirementsService
) : ViewModel() {
    private val _uiState = MutableStateFlow<RequirementsUIState>(RequirementsUIState.Verifying)
    val uiState: StateFlow<RequirementsUIState> = _uiState
    private val _requestPermission = MutableStateFlow<PermissionRequest?>(null)
    val requestPermission: StateFlow<PermissionRequest?> = _requestPermission
    private var count = 0

    init {
        viewModelScope.launch {
            requirementsService.requirementsState.collect { status ->
                when (status) {
                    RequirementsState.Ready -> {
                        delay(2_000)
                        //navigator.navigateTo(Navigator.NavTarget.Simulation)
                    }

                    is RequirementsState.Status -> _uiState.value =
                        RequirementsUIState.Status(
                            isDeveloperOptionsEnabled = status.isDeveloperOptionsEnabled,
                            isSelectedMockLocationApp = status.isSelectedMockLocationApp,
                            isAccessToFineLocationGranted = status.isAccessToFineLocationGranted,
                            isAccessToBackgroundLocationGranted = status.isAccessToBackgroundLocationGranted,
                            isAllowedToShowNotification = status.isAllowedToShowNotification,
                            shouldShowDialogRequestLocationPermissionRationale = status.shouldShowDialogRequestLocationPermissionRationale,
                            shouldShowDialogRequestBackgroundLocationPermissionRationale = status.shouldShowDialogRequestBackgroundLocationPermissionRationale,
                            shouldShowDialogRequestNotificationPermissionRationale = status.shouldShowDialogRequestNotificationPermissionRationale
                        )
                }
            }
        }
    }

    fun onRequestFineLocationPermissionClicked() {
        count++
        _requestPermission.value = PermissionRequest.PermissionFineLocation(count++)
    }

    fun onRequestBackgroundLocationPermissionClicked() {
        count++
        _requestPermission.value = PermissionRequest.PermissionBackgroundLocation(count++)
    }

    fun onRequestNotificationPermissionClicked() {
        count++
        _requestPermission.value = PermissionRequest.PermissionNotification(count++)
    }

    fun onGoToSettingsClicked() {

    }

    fun onContinueClicked() {
        navigator.navigateTo(Navigator.NavTarget.Simulation)
    }
}

sealed interface RequirementsUIState {
    data object Verifying : RequirementsUIState
    data class Status(
        val isDeveloperOptionsEnabled: Boolean,
        val isSelectedMockLocationApp: Boolean,
        val isAccessToFineLocationGranted: Boolean,
        val isAccessToBackgroundLocationGranted: Boolean,
        val isAllowedToShowNotification: Boolean,
        val shouldShowDialogRequestLocationPermissionRationale: Boolean,
        val shouldShowDialogRequestBackgroundLocationPermissionRationale: Boolean,
        val shouldShowDialogRequestNotificationPermissionRationale: Boolean
    ) : RequirementsUIState {
        fun isReady(): Boolean {
            return isDeveloperOptionsEnabled
                    && isSelectedMockLocationApp
                    && isAccessToFineLocationGranted
                    && isAccessToBackgroundLocationGranted
                    && isAllowedToShowNotification
                    && shouldShowDialogRequestLocationPermissionRationale
                    && shouldShowDialogRequestBackgroundLocationPermissionRationale
                    && shouldShowDialogRequestNotificationPermissionRationale
        }
    }
}

sealed interface PermissionRequest {
    data class PermissionFineLocation(val count: Int = 0) : PermissionRequest
    data class PermissionBackgroundLocation(val count: Int = 0) : PermissionRequest
    data class PermissionNotification(val count: Int = 0) : PermissionRequest
}