package de.p72b.mocklation.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FeatureEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "name") val name: String?
)