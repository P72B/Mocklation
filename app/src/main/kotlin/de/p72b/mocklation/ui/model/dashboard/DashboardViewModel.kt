package de.p72b.mocklation.ui.model.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUIState>(DashboardUIState.Loading)
    val uiState: StateFlow<DashboardUIState> = _uiState

    private fun updateUi(uiState: DashboardUIState) {
        _uiState.value = uiState
    }

}

sealed interface DashboardUIState {
    data object Loading : DashboardUIState
}