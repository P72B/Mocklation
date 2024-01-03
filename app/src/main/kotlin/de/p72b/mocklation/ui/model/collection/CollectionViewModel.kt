package de.p72b.mocklation.ui.model.collection

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.PreferencesRepository
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.usecase.GetCollectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val getCollectionUseCase: GetCollectionUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel(), LifecycleEventObserver {

    private val _uiState = MutableStateFlow<CollectionUIState>(CollectionUIState.Loading)
    val uiState: StateFlow<CollectionUIState> = _uiState

    private fun updateUi(uiState: CollectionUIState) {
        _uiState.value = uiState
    }

    fun onItemClicked(feature: Feature) {
        val currentSelectedFeature = preferencesRepository.getSelectedFeature()
        if (currentSelectedFeature == feature.uuid) {
            preferencesRepository.setSelectedFeature(null)
        } else {
            preferencesRepository.setSelectedFeature(feature.uuid)
        }
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
}