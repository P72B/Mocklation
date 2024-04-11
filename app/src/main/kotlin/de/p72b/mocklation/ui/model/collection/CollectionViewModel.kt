package de.p72b.mocklation.ui.model.collection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.PreferencesRepository
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.service.ForegroundServiceInteractor
import de.p72b.mocklation.service.StatusEvent
import de.p72b.mocklation.usecase.DeleteFeatureUseCase
import de.p72b.mocklation.usecase.GetCollectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val getCollectionUseCase: GetCollectionUseCase,
    private val deleteFeatureUseCase: DeleteFeatureUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val simulationService: ForegroundServiceInteractor,
) : ViewModel(), LifecycleEventObserver {

    private val _uiState = MutableStateFlow<CollectionUIState>(CollectionUIState.Loading)
    val uiState: StateFlow<CollectionUIState> = _uiState

    private fun updateUi(uiState: CollectionUIState) {
        _uiState.value = uiState
    }

    fun onItemClicked(feature: Feature) {
        val currentSelectedFeature = preferencesRepository.getSelectedFeature()
        if (currentSelectedFeature != null) {
            viewModelScope.launch {
                if (simulationService.status.value != StatusEvent.Stop) {
                    if (preferencesRepository.getShouldAskAgainToStopSimulationService()) {
                        updateUi(CollectionUIState.ShowSimulationCancelDialog(feature))
                    } else {
                        simulationService.doStop()
                        consumeItemClick(feature, currentSelectedFeature)
                    }
                } else {
                    consumeItemClick(feature, currentSelectedFeature)
                }
            }
        } else {
            consumeItemClick(feature, null)
        }
    }

    private fun consumeItemClick(feature: Feature, currentSelectedFeature: String?) {
        if (currentSelectedFeature == feature.uuid) {
            preferencesRepository.setSelectedFeature(null)
        } else {
            preferencesRepository.setSelectedFeature(feature.uuid)
        }
        fetchDatabaseData()
    }

    fun onDelete(feature: Feature) {
        viewModelScope.launch {
            val result = deleteFeatureUseCase.invoke(feature)
            when (result.status) {
                Status.SUCCESS -> {
                    if (preferencesRepository.getSelectedFeature() == feature.uuid) {
                        preferencesRepository.setSelectedFeature(null)
                    }
                    fetchDatabaseData()
                }

                Status.ERROR -> updateUi(CollectionUIState.Error)
            }
        }
    }

    fun onConfirmToCancelOngoingSimulation(feature: Feature, shouldAskAgain: Boolean = true) {
        preferencesRepository.setShouldAskAgainToStopSimulationService(shouldAskAgain)
        val currentSelectedFeature = preferencesRepository.getSelectedFeature()
        simulationService.doStop()
        consumeItemClick(feature, currentSelectedFeature)
    }

    fun onDismissToCancelOngoingSimulation() {
        // do nothing here so far
        fetchDatabaseData()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> fetchDatabaseData()
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

    private fun fetchDatabaseData() {
        viewModelScope.launch {
            val result = getCollectionUseCase.invoke()
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let {
                        if (it.isEmpty()) {
                            updateUi(CollectionUIState.Empty)
                        } else {
                            updateUi(
                                CollectionUIState.Data(
                                    it,
                                    preferencesRepository.getSelectedFeature()
                                )
                            )
                        }

                        return@launch
                    }
                    updateUi(CollectionUIState.Error)
                }

                Status.ERROR -> updateUi(CollectionUIState.Error)
            }
        }
    }
}

sealed interface CollectionUIState {
    data object Loading : CollectionUIState
    data object Empty : CollectionUIState
    data class Data(val items: List<Feature>, val selectedItem: String? = null) : CollectionUIState
    data object Error : CollectionUIState
    data class ShowSimulationCancelDialog(val feature: Feature) : CollectionUIState
}