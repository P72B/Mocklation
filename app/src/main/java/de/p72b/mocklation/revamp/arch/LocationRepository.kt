package de.p72b.mocklation.revamp.arch

import androidx.lifecycle.LiveData
import de.p72b.mocklation.revamp.room.AppDatabase.Companion.locationsDb
import de.p72b.mocklation.revamp.room.LocationItem


class LocationRepository {

    private val database = locationsDb.build()
    private val locationItemDao = database.locationItemDao()

    fun getAll(): LiveData<List<LocationItem>> {
        return locationItemDao.allLiveData
    }
}