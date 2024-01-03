package de.p72b.mocklation.data.room

import androidx.room.TypeConverter
import de.p72b.mocklation.data.Geometry
import de.p72b.mocklation.data.LatLng
import de.p72b.mocklation.data.Node
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class Converters {
    @TypeConverter
    fun fromList(value : MutableList<Node>) = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<MutableList<Node>>(value)

    @TypeConverter
    fun fromLatLng(value: LatLng) = Json.encodeToString(value)

    @TypeConverter
    fun toLatLng(value: String) = Json.decodeFromString<LatLng>(value)

    @TypeConverter
    fun fromGeometry(value: Geometry) = Json.encodeToString(value)

    @TypeConverter
    fun toGeometry(value: String) = Json.decodeFromString<Geometry>(value)
}