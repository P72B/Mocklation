package de.p72b.mocklation.data

import java.util.Date
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class MockFeature(
    val uuid: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var nodes: MutableList<Node> = mutableListOf(),
    var ways: MutableList<Way> = mutableListOf(),
    var bbox: List<Double>? = null,
    var totalLengthInMeter: Double? = null,
    var avgSpeedInKmh: Double? = null,
    var totalTravelTimeInSeconds: Double? = null,
    var lastModified: Long = Date().time
)

@Serializable
data class Node(
    val id: Int,
    val isTunnel: Boolean = false,
    var speedInKmh: Double = 60.0,
    val accuracyInMeter: Float = 0f,
    val geometry: LatLng
)

@Serializable
data class Way(
    val id: Int,
    val isTunnel: Boolean = false,
    var lengthInMeter: Double? = null,
    var speedInKmh: Double = 60.0,
    var travelTimeInSeconds: Double? = null,
    val accuracyInMeter: Float = 0f,
    val geometry: LatLng
)

@Serializable
data class LatLng(var latitude: Double, var longitude: Double)