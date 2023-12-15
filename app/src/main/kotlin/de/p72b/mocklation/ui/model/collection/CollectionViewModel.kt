package de.p72b.mocklation.ui.model.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.usecase.GetCollectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val getCollectionUseCase: GetCollectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionUIState>(CollectionUIState.Loading)
    val uiState: StateFlow<CollectionUIState> = _uiState

    init {
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
                                    it
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

    private fun updateUi(uiState: CollectionUIState) {
        _uiState.value = uiState
    }

}

sealed interface CollectionUIState {
    data object Loading : CollectionUIState
    data object Empty : CollectionUIState
    data class Data(val items: List<Feature>) : CollectionUIState
    data object Error : CollectionUIState
}