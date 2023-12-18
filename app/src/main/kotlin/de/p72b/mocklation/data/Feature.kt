package de.p72b.mocklation.data

data class Feature(
    val name: String? = null,
    val nodes: MutableList<Node> = mutableListOf()
)

data class Node(val id: Int, val geometry: Geometry)
data class Geometry(val latLng: LatLng)
data class LatLng(val latitude: Double, val longitude: Double)