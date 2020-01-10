package de.p72b.mocklation.revamp.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.maps.android.data.Geometry

@Entity(tableName = "locations")
data class LocationItem(@PrimaryKey
                        @ColumnInfo(name = "code") val code: String = "",
                        @ColumnInfo(name = "title") var title: String = "",
                        @ColumnInfo(name = "geom") var geometry: Geometry<Any>? = null,
                        @ColumnInfo(name = "accuracy") var accuracy: Int = 0,
                        @ColumnInfo(name = "speed") var speed: Int = 0,
                        @ColumnInfo(name = "favorite") var favorite: Boolean = false,
                        @ColumnInfo(name = "color") var color: Int = 0,
                        @ColumnInfo(name = "selected") var selected: Boolean = false)