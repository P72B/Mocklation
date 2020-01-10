package de.p72b.mocklation.revamp.arch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.p72b.mocklation.revamp.room.LocationItem
import org.koin.core.KoinComponent
import org.koin.core.inject


class LocationViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val repository: LocationRepository by inject()
    val all: LiveData<List<LocationItem>>

    init {
        all = repository.getAll()
    }

    fun isEmpty(): Boolean {
        if (all.value == null) {
            return true
        }
        return all.value!!.isEmpty()
    }

    fun changeSelection(item: LocationItem, state: Boolean) {
        all.value?.let {
            it.forEach { singleItem ->
                if (singleItem.code == item.code) {
                    singleItem.selected = state
                } else {
                    singleItem.selected = false
                }
            }
            repository.save(it)
        }
    }
}