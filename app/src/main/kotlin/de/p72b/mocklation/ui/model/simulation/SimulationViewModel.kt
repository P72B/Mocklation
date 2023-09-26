package de.p72b.mocklation.ui.model.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.service.ForegroundServiceInteractor
import de.p72b.mocklation.service.StatusEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SimulationViewModel(private val simulationService: ForegroundServiceInteractor) : ViewModel() {

    private val _uiState = MutableStateFlow<SimulationUIState>(SimulationUIState.Loading)
    val uiState: StateFlow<SimulationUIState> = _uiState

    init {
        viewModelScope.launch {
            simulationService.status.collect { status ->
                when (status) {
                    StatusEvent.Stop -> updateUi(SimulationUIState.StoppedSimulation)
                    StatusEvent.Pause -> TODO()
                    StatusEvent.Play -> updateUi(SimulationUIState.RunningSimulation)
                }
            }
        }
    }

    fun runSimulation() {
        updateUi(SimulationUIState.Loading)
        simulationService.doPlay()
    }

    fun stopSimulation() {
        updateUi(SimulationUIState.Loading)
        simulationService.doStop()
    }

    private fun updateUi(uiState: SimulationUIState) {
        _uiState.value = uiState
    }

}

sealed interface SimulationUIState {
    data object Loading : SimulationUIState
    data object StoppedSimulation : SimulationUIState
    data object RunningSimulation : SimulationUIState
    data class Error(val throwable: Throwable) : SimulationUIState
    data class Success(val data: List<String>) : SimulationUIState
}