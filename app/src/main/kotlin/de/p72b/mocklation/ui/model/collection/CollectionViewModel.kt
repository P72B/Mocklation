package de.p72b.mocklation.ui.model.collection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CollectionViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionUIState>(CollectionUIState.Loading)
    val uiState: StateFlow<CollectionUIState> = _uiState

    private fun updateUi(uiState: CollectionUIState) {
        _uiState.value = uiState
    }

}

sealed interface CollectionUIState {
    data object Loading : CollectionUIState
}