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

    init {
        viewModelScope.launch {
            requirementsService.requirementsState.collect { status ->
                when (status) {
                    RequirementsState.Ready -> {
                        delay(2_000)
                        navigator.navigateTo(Navigator.NavTarget.Simulation)
                    }
                    is RequirementsState.Status -> _uiState.value =
                        RequirementsUIState.Status(
                            status.isDeveloperOptionsEnabled,
                            status.isSelectedMockLocationApp,
                            status.isAccessToFineLocationGranted,
                            status.isAccessToBackgroundLocationGranted,
                            status.isAllowedToShowNotification
                        )
                }
            }
        }
    }

    fun onRequestFineLocationPermissionClicked() {
        _requestPermission.value = PermissionRequest.PermissionFineLocation
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
        val isAllowedToShowNotification: Boolean
    ) : RequirementsUIState
}

sealed interface PermissionRequest {
    data object PermissionFineLocation: PermissionRequest
    data object PermissionBackgroundLocation: PermissionRequest
}