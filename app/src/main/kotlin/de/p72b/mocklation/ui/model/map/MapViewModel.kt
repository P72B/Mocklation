package de.p72b.mocklation.ui.model.map

import androidx.lifecycle.ViewModel
import de.p72b.mocklation.data.Feature
import de.p72b.mocklation.data.Geometry
import de.p72b.mocklation.data.LatLng
import de.p72b.mocklation.data.Node
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {

    private val feature = Feature()

    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.FeatureData(feature))
    val uiState: StateFlow<MapUIState> = _uiState

    fun onMapLongClick(lat: Double, lng: Double) {
        val node = Node(
            id = determineNextNodeId(feature.nodes),
            geometry = Geometry(LatLng(lat, lng))
        )
        feature.nodes.add(node)

        _uiState.value = MapUIState.FeatureData(feature)
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
    data class FeatureData(val feature: Feature) : MapUIState
}