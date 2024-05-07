package de.p72b.mocklation.parser

import android.content.Context
import com.google.maps.android.data.geojson.GeoJsonParser
import de.p72b.mocklation.data.MockFeature
import org.json.JSONObject

class TrackImport(private val context: Context) {

    fun read(): List<MockFeature> {
        val text =
            context.resources.openRawResource(R.raw.sample).bufferedReader().use { it.readText() }
        val geoJsonParser = GeoJsonParser(JSONObject(text))
        val result = mutableListOf<MockFeature>()
        for (feature in geoJsonParser.features) {
            /*
            if (feature.hasGeometry()) {
                var isTunnel = false
                var speed = 60
                var accuracy = 12f
                for (prop in feature.properties) {
                    val pair = prop as Map.Entry<*, *>
                    if (pair.key == "isTunnel" && pair.value == "1") {
                        isTunnel = true
                    }
                    if (pair.key == "speed") {
                        speed = pair.value as Int
                    }
                    if (pair.key == "accuracy") {
                        accuracy = pair.value as Float
                    }
                }
                when (feature.geometry) {
                    is LineString -> {
                        for (item in (feature.geometry as LineString).geometryObject) {
                            result.add(
                                MockFeature(
                                    speedInKmh = speed,
                                    location = Location(LocationManager.GPS_PROVIDER).apply {
                                        latitude = item.latitude
                                        longitude = item.longitude
                                        this.accuracy = accuracy
                                    },
                                    isTunnel = isTunnel
                                )
                            )
                        }
                    }
                    is Point -> {
                        val latLng = (feature.geometry as Point).geometryObject
                        result.add(
                            MockFeature(
                                speedInKmh = speed,
                                location = Location(LocationManager.GPS_PROVIDER).apply {
                                    latitude = latLng.latitude
                                    longitude = latLng.longitude
                                    this.accuracy = accuracy
                                },
                                isTunnel = isTunnel
                            )
                        )
                    }
                    else -> {
                        // TODO not supported
                    }
                }
            }
            */
        }
        return emptyList()
    }
}