package de.p72b.mocklation.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FeatureDao {
    @Query("SELECT * FROM featureEntity")
    fun getAll(): List<FeatureEntity>

    @Query("SELECT * FROM featureEntity WHERE uid IN (:coinIds)")
    fun loadAllByIds(coinIds: IntArray): List<FeatureEntity>

    @Query("SELECT * FROM featureEntity WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): FeatureEntity

    @Insert
    fun insertAll(vararg coins: FeatureEntity)

    @Delete
    fun delete(coin: FeatureEntity)
}