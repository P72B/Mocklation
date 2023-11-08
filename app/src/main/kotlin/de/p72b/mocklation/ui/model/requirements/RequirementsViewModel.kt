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
                            status.isSelectedMockLocationApp
                        )
                }
            }
        }
    }

}

sealed interface RequirementsUIState {
    data object Verifying : RequirementsUIState
    data class Status(
        val isDeveloperOptionsEnabled: Boolean,
        val isSelectedMockLocationApp: Boolean
    ) : RequirementsUIState
}