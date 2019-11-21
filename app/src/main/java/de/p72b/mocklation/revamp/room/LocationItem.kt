package de.p72b.mocklation.revamp.room

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.maps.android.data.Geometry
import com.google.maps.android.data.geojson.GeoJsonParser
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Entity(tableName = "locations")
@Parcelize
data class LocationItem(@PrimaryKey
                        @ColumnInfo(name = "code") val code: String = "",
                        @ColumnInfo(name = "displayed_name") var displayedName: String = "",
                        @ColumnInfo(name = "geo_json") var geoJson: String = "{}",
                        @ColumnInfo(name = "accuracy") var accuracy: Int = 0,
                        @ColumnInfo(name = "speed") var speed: Int = 0,
                        @ColumnInfo(name = "favorite") var favorite: Boolean = false,
                        @ColumnInfo(name = "color") var color: Int = 0) : Parcelable {
    fun getGeometry(): Geometry<Any>? {
        return try {
            GeoJsonParser.parseGeometry(JSONObject(geoJson))
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }
}