package de.p72b.mocklation.revamp.arch

import androidx.lifecycle.LiveData
import de.p72b.mocklation.revamp.room.AppDatabase.Companion.locationsDb
import de.p72b.mocklation.revamp.room.LocationItem
import de.p72b.mocklation.util.Logger
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


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

    fun save(list: List<LocationItem>) {
        val resultToBeIgnored = Completable.fromAction { locationItemDao.insertAll(list) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Logger.d("p72b", "updated the database")
                }
    }

    fun save(item: LocationItem) {
        val resultToBeIgnored = Completable.fromAction { locationItemDao.insertAll(item) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Logger.d("p72b", "updated the database")
                }
    }

    fun delete(item: LocationItem) {
        val resultToBeIgnored = Completable.fromAction { locationItemDao.delete(item) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Logger.d("p72b", "updated the database")
                }
    }
}