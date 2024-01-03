package de.p72b.mocklation.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeatureDao {
    @Query("SELECT * FROM featureEntity")
    fun getAll(): List<FeatureEntity>

    @Query("SELECT * FROM featureEntity WHERE uid IN (:coinIds)")
    fun loadAllByIds(coinIds: IntArray): List<FeatureEntity>

    @Query("SELECT * FROM featureEntity WHERE uid LIKE :id LIMIT 1")
    fun findById(id: Int): FeatureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg feature: FeatureEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feature: FeatureEntity)

    @Delete
    fun delete(coin: FeatureEntity)
}