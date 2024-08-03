package de.p72b.mocklation.ui.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.LatLng
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.usecase.MapBoundsUseCase
import de.p72b.mocklation.usecase.SetFeatureUseCase
import de.p72b.mocklation.util.Logger
import de.p72b.mocklation.util.StatisticsCalculator
import de.p72b.mocklation.util.TWO_DIGITS_COUNTRY_CODE_LOCATION_LIST
import de.p72b.mocklation.util.roundTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MapViewModel(
    private val setFeatureUseCase: SetFeatureUseCase,
    private val mapBoundsUseCase: MapBoundsUseCase
) : ViewModel() {

    private val feature = MockFeature()
    private val stats = StatisticsCalculator(feature)
    private var savedAt: Long? = null

    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val uiState: StateFlow<MapUIState> = _uiState
    private var selectedId: Int? = null

    init {
        Logger.d(msg = "MapViewModel init")
        viewModelScope.launch {
            val result = mapBoundsUseCase.invoke()
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let {
                        val update = MapUIState.CameraUpdate(animate = false)
                        if (it.coordinates.size == 1) {
                            update.point = MapUIState.Point(
                                centerLatitude = it.coordinates[0].x,
                                centerLongitude = it.coordinates[0].y
                            )
                        } else if (it.coordinates.isNotEmpty()) {
                            update.bounds = MapUIState.Bounds(
                                southWestLatitude = it.coordinates[0].x,
                                southWestLongitude = it.coordinates[0].y,
                                northEastLatitude = it.coordinates[2].x,
                                northEastLongitude = it.coordinates[2].y,
                            )
                        }
                        _uiState.value = MapUIState.FeatureDataUpdate(
                            cameraUpdate = update,
                            selectedId = selectedId,
                            feature = feature,
                            tstamp = Date().time,
                            statisticsViewData = getViewData()
                        )
                    }
                }

                Status.ERROR -> {
                    // can be ignored
                }
            }
        }
    }

    fun onMarkerDragEnd(node: Node, newLatitude: Double, newLongitude: Double) {
        Logger.d(msg = "MapViewModel onMarkerDragEnd")
        if (feature.nodes.contains(node).not()) {
            return
        }
        feature.nodes.find { node.id == it.id }?.let {
            it.geometry.latitude = newLatitude
            it.geometry.longitude = newLongitude
        }

        stats.setFeature(feature)
        selectedId = node.id
        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = selectedId,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    fun onMapClick(lat: Double, lng: Double) {
        Logger.d(msg = "MapViewModel onMapClick")
        selectedId = null
        val targetCoords: LatLng = createLatLng(lat, lng)
        feature.nodes.find {
            it.geometry.latitude == targetCoords.latitude
                    && it.geometry.longitude == targetCoords.longitude
        }?.let {
            selectedId = it.id
        }
        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = selectedId,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    private fun createLatLng(lat: Double, lng: Double): LatLng {
        return LatLng(lat.roundTo(6), lng.roundTo(6))
    }

    fun onMapLongClick(lat: Double, lng: Double) {
        Logger.d(msg = "MapViewModel onMapLongClick")
        val currentId = determineNextNodeId(feature.nodes)
        val node = Node(
            id = currentId,
            accuracyInMeter = 6.0f,
            geometry = createLatLng(lat, lng)
        )
        feature.nodes.add(node)
        stats.setFeature(feature)
        selectedId = node.id

        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = selectedId,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    fun getDefaultMapCameraLocation(twoDigitCountryCode: String?): LatLng {
        twoDigitCountryCode?.let { code ->
            TWO_DIGITS_COUNTRY_CODE_LOCATION_LIST[code.uppercase()]?.let {
                return LatLng(latitude = it.latitude, longitude = it.longitude)
            }
        }
        return LatLng(1.3588227, 103.8742114)
    }

    fun onNodeListItemClicked(node: Node) {
        Logger.d(msg = "MapViewModel onNodeListItemClicked")
        selectedId = node.id
        _uiState.value = MapUIState.FeatureDataUpdate(
            cameraUpdate = MapUIState.CameraUpdate(
                point = MapUIState.Point(node.geometry.latitude, node.geometry.longitude)
            ),
            selectedId = selectedId,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    fun onNodeClicked(node: Node) {
        Logger.d(msg = "MapViewModel onNodeClicked")
        selectedId = node.id
        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = selectedId,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    fun onDeleteClicked(node: Node) {
        Logger.d(msg = "MapViewModel onDeleteClicked")
        if (node.id == selectedId) {
            selectedId = null
        }
        feature.nodes.remove(node)
        stats.setFeature(feature)

        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = selectedId,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    fun onSaveClicked() {
        Logger.d(msg = "MapViewModel onSaveClicked")
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

    private fun getViewData(): MapUIState.StatisticsViewData {
        return MapUIState.StatisticsViewData(
            pathLength = "${stats.pathLengthInMeter.roundTo(2)} m",
            totalTravelTime = "${stats.totalTravelTimeInSeconds.roundTo(1)} s",
            avgSpeed = "Ø ${(stats.avgSpeedInKmh * 3.6).toInt()} km/h"
        )
    }
}

sealed interface MapUIState {
    data object Loading : MapUIState
    data class FeatureDataUpdate(
        val cameraUpdate: CameraUpdate = CameraUpdate(),
        val selectedId: Int? = null,
        val feature: MockFeature,
        val tstamp: Long? = null,
        val statisticsViewData: StatisticsViewData
    ) : MapUIState

    data class CameraUpdate(
        var point: Point? = null,
        var bounds: Bounds? = null,
        var animate: Boolean = true,
    )

    data class Point(
        val centerLatitude: Double,
        val centerLongitude: Double,
    )

    data class Bounds(
        val southWestLatitude: Double,
        val southWestLongitude: Double,
        val northEastLatitude: Double,
        val northEastLongitude: Double
    )

    data class StatisticsViewData(
        val pathLength: String,
        val totalTravelTime: String,
        val avgSpeed: String
    )
}