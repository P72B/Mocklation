package de.p72b.mocklation.ui.model.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.service.ForegroundServiceInteractor
import de.p72b.mocklation.service.RequirementsService
import de.p72b.mocklation.service.RequirementsState
import de.p72b.mocklation.service.StatusEvent
import de.p72b.mocklation.ui.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SimulationViewModel(
    private val navigator: Navigator,
    private val simulationService: ForegroundServiceInteractor,
    private val requirementsService: RequirementsService
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimulationUIState>(SimulationUIState.Loading)
    val uiState: StateFlow<SimulationUIState> = _uiState

    init {
        viewModelScope.launch {
            simulationService.status.collect { status ->
                when (status) {
                    StatusEvent.Stop -> updateUi(SimulationUIState.StoppedSimulation)
                    StatusEvent.Pause -> updateUi(SimulationUIState.PausedSimulation)
                    StatusEvent.Play -> updateUi(SimulationUIState.RunningSimulation)
                }
            }
        }
    }

    fun runSimulation() {
        viewModelScope.launch {
            if (requirementsService.requirementsState.value == RequirementsState.Ready) {
                updateUi(SimulationUIState.Loading)
                simulationService.doPlay()
            } else {
                navigator.navigateTo(Navigator.NavTarget.Requirements)
            }
        }
    }

    fun stopSimulation() {
        updateUi(SimulationUIState.Loading)
        simulationService.doStop()
    }

    fun pauseSimulation() {
        simulationService.doPause()
    }

    fun resumeSimulation() {
        simulationService.doResume()
    }

    private fun updateUi(uiState: SimulationUIState) {
        _uiState.value = uiState
    }

}

sealed interface SimulationUIState {
    data object Loading : SimulationUIState
    data object StoppedSimulation : SimulationUIState
    data object RunningSimulation : SimulationUIState
    data object PausedSimulation : SimulationUIState
    data class Error(val throwable: Throwable) : SimulationUIState
    data class Success(val data: List<String>) : SimulationUIState
}