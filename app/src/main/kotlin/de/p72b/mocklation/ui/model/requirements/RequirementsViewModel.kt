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
    private val _action = MutableStateFlow<Action?>(null)
    val action: StateFlow<Action?> = _action
    private var count = 0

    init {
        viewModelScope.launch {
            requirementsService.requirementsState.collect { status ->
                when (status) {
                    RequirementsState.Ready -> {
                        _uiState.value = RequirementsUIState.Status(
                            isDeveloperOptionsEnabled = true,
                            isSelectedMockLocationApp = true,
                            isAccessToFineLocationGranted = true,
                            isAccessToBackgroundLocationGranted = true,
                            isAllowedToShowNotification = true,
                            shouldShowDialogRequestLocationPermissionRationale = true,
                            shouldShowDialogRequestBackgroundLocationPermissionRationale = true,
                            shouldShowDialogRequestNotificationPermissionRationale = true,
                            canContinue = true
                        )
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
                            shouldShowDialogRequestNotificationPermissionRationale = status.shouldShowDialogRequestNotificationPermissionRationale,
                            canContinue = false
                        )
                }
            }
        }
    }

    fun onRequestFineLocationPermissionClicked() {
        count++
        _requestPermission.value = PermissionRequest.PermissionFineLocation(count++)
        _requestPermission.value = null
    }

    fun onRequestBackgroundLocationPermissionClicked() {
        count++
        _requestPermission.value = PermissionRequest.PermissionBackgroundLocation(count++)
        _requestPermission.value = null
    }

    fun onRequestNotificationPermissionClicked() {
        count++
        _requestPermission.value = PermissionRequest.PermissionNotification(count++)
        _requestPermission.value = null
    }

    fun onGoToAppSettingsClicked() {
        _action.value = Action.OpenAppSettings
        _action.value = null
    }

    fun onGoToDeveloperSettingsClicked() {
        _action.value = Action.OpenDeveloperSettings
        _action.value = null
    }

    fun onGoToAboutPhoneSettingsClicked() {
        _action.value = Action.OpenAboutPhoneSettings
        _action.value = null
    }

    fun onContinueClicked() {
        navigator.navigateTo(Navigator.NavTarget.Dashboard)
    }
}

sealed interface RequirementsUIState {
    data object Verifying : RequirementsUIState
    data class Status(
        val isDeveloperOptionsEnabled: Boolean = false,
        val isSelectedMockLocationApp: Boolean = false,
        val isAccessToFineLocationGranted: Boolean = false,
        val isAccessToBackgroundLocationGranted: Boolean = false,
        val isAllowedToShowNotification: Boolean = false,
        val shouldShowDialogRequestLocationPermissionRationale: Boolean = false,
        val shouldShowDialogRequestBackgroundLocationPermissionRationale: Boolean = false,
        val shouldShowDialogRequestNotificationPermissionRationale: Boolean = false,
        val canContinue: Boolean = false
    ) : RequirementsUIState
}

sealed interface PermissionRequest {
    data class PermissionFineLocation(val count: Int = 0) : PermissionRequest
    data class PermissionBackgroundLocation(val count: Int = 0) : PermissionRequest
    data class PermissionNotification(val count: Int = 0) : PermissionRequest
}

sealed interface Action {
    data object OpenAppSettings : Action
    data object OpenDeveloperSettings : Action
    data object OpenAboutPhoneSettings : Action
}