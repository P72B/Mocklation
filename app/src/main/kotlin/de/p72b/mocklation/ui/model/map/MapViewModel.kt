package de.p72b.mocklation.ui.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.p72b.mocklation.data.MockFeature
import de.p72b.mocklation.data.LatLng
import de.p72b.mocklation.data.Node
import de.p72b.mocklation.data.util.Status
import de.p72b.mocklation.usecase.MapBoundsUseCase
import de.p72b.mocklation.usecase.SetFeatureUseCase
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

    private val pointOnly = false
    private val feature = MockFeature()
    private val stats = StatisticsCalculator(feature)
    private var savedAt: Long? = null

    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val uiState: StateFlow<MapUIState> = _uiState

    init {
        viewModelScope.launch {
            val result = mapBoundsUseCase.invoke()
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let {
                        val update = MapUIState.CameraUpdate()
                        if (it.coordinates.size == 1) {
                            update.point = MapUIState.Point(
                                centerLatitude = it.coordinates[0].x,
                                centerLongitude = it.coordinates[0].y
                            )
                        } else {
                            update.bounds = MapUIState.Bounds(
                                southWestLatitude = it.coordinates[0].x,
                                southWestLongitude = it.coordinates[0].y,
                                northEastLatitude = it.coordinates[2].x,
                                northEastLongitude = it.coordinates[2].y,
                            )
                        }
                        _uiState.value = update
                    }
                }

                Status.ERROR -> {
                    // can be ignored
                }
            }
        }
    }

    fun onMarkerDragEnd(node: Node, newLatitude: Double, newLongitude: Double) {
        if (feature.nodes.contains(node).not()) {
            return
        }
        feature.nodes.find { node.id == it.id }?.let {
            it.geometry.latitude = newLatitude
            it.geometry.longitude = newLongitude
        }

        stats.setFeature(feature)
        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = node.id,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
        )
    }

    fun onMapLongClick(lat: Double, lng: Double) {
        if (pointOnly && feature.nodes.isEmpty().not()) {
            return
        }
        val currentId = determineNextNodeId(feature.nodes)
        val node = Node(
            id = currentId,
            accuracyInMeter = 6.0f,
            geometry = LatLng(lat.roundTo(6), lng.roundTo(6))
        )
        feature.nodes.add(node)
        stats.setFeature(feature)

        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = currentId,
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

    fun onNodeClicked(node: Node) {
        _uiState.value = MapUIState.FeatureDataUpdate(
            selectedId = node.id,
            feature = feature,
            tstamp = Date().time,
            statisticsViewData = getViewData()
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
        val selectedId: Int? = null,
        val feature: MockFeature,
        val tstamp: Long? = null,
        val statisticsViewData: StatisticsViewData
    ) : MapUIState

    data class CameraUpdate(
        var point: Point? = null,
        var bounds: Bounds? = null
    ) : MapUIState

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