package de.p72b.mocklation.ui.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.Geometry
import de.p72b.mocklation.data.LatLng
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.usecase.SetFeatureUseCase
import de.p72b.mocklation.util.roundTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MapViewModel(
    private val setFeatureUseCase: SetFeatureUseCase
) : ViewModel() {

    private val pointOnly = false
    private val feature = Feature()
    private var savedAt: Long? = null

    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val uiState: StateFlow<MapUIState> = _uiState

    fun onMapLongClick(lat: Double, lng: Double) {
        if (pointOnly && feature.nodes.isEmpty().not()) {
            return
        }
        val currentId = determineNextNodeId(feature.nodes)
        val node = Node(
            id = currentId,
            geometry = Geometry(LatLng(lat.roundTo(6), lng.roundTo(6)   ))
        )
        feature.nodes.add(node)

        _uiState.value = MapUIState.FeatureData(
            selectedId = currentId,
            feature = feature,
            tstamp = Date().time
        )
    }

    fun onNodeClicked(node: Node) {
        _uiState.value = MapUIState.FeatureData(
            selectedId = node.id,
            feature = feature,
            tstamp = Date().time
        )
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            val previousLastModified = feature.lastModified
            feature.lastModified = Date().time
            val result = setFeatureUseCase.invoke(feature)
            when (result.status) {
                Status.SUCCESS -> {
                    savedAt = feature.lastModified
                }

                Status.ERROR -> {
                    feature.lastModified = previousLastModified
                    // TODO print error
                }
            }
        }
    }

    private fun determineNextNodeId(list: List<Node>): Int {
        var resultId = 1
        if (list.isEmpty()) {
            return resultId
        }
        for (node in list) {
            if (resultId <= node.id) {
                resultId = node.id + 1
            }
        }
        return resultId
    }
}

sealed interface MapUIState {
    data object Loading : MapUIState
    data class FeatureData(
        val selectedId: Int? = null,
        val feature: Feature,
        val tstamp: Long? = null,
    ) : MapUIState
}