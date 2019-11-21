package de.p72b.mocklation.revamp.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface LocationItemDao {
    @get:Query("SELECT * FROM locations")
    val all: Maybe<List<LocationItem>>

    @get:Query("SELECT * FROM locations")
    val allLiveData: LiveData<List<LocationItem>>

    @Query("SELECT * FROM locations where code = :code")
    fun findByCode(code: String): Single<LocationItem>

    @Query("SELECT * FROM locations where displayed_name = :displayedName")
    fun findByDisplayedName(displayedName: String): Maybe<List<LocationItem>>

    @Insert
    fun insertAll(vararg locationItems: LocationItem)

    @Delete
    fun delete(locationItem: LocationItem)

    @Update
    fun updateLocationItems(vararg locationItems: LocationItem)
}