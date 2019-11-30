package de.p72b.mocklation.revamp.arch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.p72b.mocklation.revamp.room.LocationItem
import org.koin.core.KoinComponent
import org.koin.core.inject


class LocationViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val repository: LocationRepository by inject()

    val items: LiveData<List<LocationItem>>
        get() = repository.getAll()
}