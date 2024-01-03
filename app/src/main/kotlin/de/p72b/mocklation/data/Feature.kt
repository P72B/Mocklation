package de.p72b.mocklation.data

import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class Feature(
    val uuid: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var nodes: MutableList<Node> = mutableListOf(),
    var lastModified: Long = Date().time
)

@Serializable
data class Node(val id: Int, val geometry: Geometry)

@Serializable
data class Geometry(val latLng: LatLng)

@Serializable
data class LatLng(val latitude: Double, val longitude: Double)