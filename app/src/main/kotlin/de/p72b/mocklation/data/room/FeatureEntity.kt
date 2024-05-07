package de.p72b.mocklation.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.p72b.mocklation.data.Node

@Entity
data class FeatureEntity(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "nodes") val nodes: MutableList<Node>,
    @ColumnInfo(name = "lastModified") val lastModified: Long
)