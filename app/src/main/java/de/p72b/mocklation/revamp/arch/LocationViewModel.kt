package de.p72b.mocklation.revamp.arch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.p72b.mocklation.revamp.room.LocationItem
import org.koin.core.KoinComponent
import org.koin.core.inject


class LocationViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val repository: LocationRepository by inject()
    val lastDeletedItem = MutableLiveData<LocationItem>()
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

    fun delete(item: LocationItem) {
        repository.delete(item)
        item.selected = false
        lastDeletedItem.value = item
    }

    fun undo() {
        lastDeletedItem.value?.let {
            repository.save(it)
        }
    }
}