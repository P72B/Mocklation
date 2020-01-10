package de.p72b.mocklation.revamp.arch

import androidx.lifecycle.LiveData
import de.p72b.mocklation.revamp.room.AppDatabase.Companion.locationsDb
import de.p72b.mocklation.revamp.room.LocationItem


class LocationRepository {

    private var all: LiveData<List<LocationItem>>
    private val database = locationsDb.build()
    private val locationItemDao = database.locationItemDao()

    init {
        all = locationItemDao.allLiveData
    }

    fun getAll(): LiveData<List<LocationItem>> {
        return all
    }
}