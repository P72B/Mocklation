package de.p72b.mocklation.revamp.room

import androidx.room.TypeConverter
import com.google.maps.android.data.Geometry
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPoint
import org.json.JSONException
import org.json.JSONObject

class GeometryTypeConverter {
    @TypeConverter
    fun toGeometry(value: String): Geometry<Any>? {
        return try {
            GeoJsonParser.parseGeometry(JSONObject(value))
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    @TypeConverter
    fun toString(value: Geometry<Any>): String? {
        if (value is GeoJsonPoint) {
            // this could be directly on the Geometry interface of the android-maps-utils library e.g. create pull request
            return "{'type':'Point','coordinates':[${value.coordinates.longitude},${value.coordinates.latitude}]}"
        }
        return null
    }
}