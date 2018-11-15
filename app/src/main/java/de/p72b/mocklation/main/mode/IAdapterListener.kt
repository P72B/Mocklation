package de.p72b.mocklation.main.mode

import android.view.View
import de.p72b.mocklation.service.room.LocationItem

interface IAdapterListener : View.OnClickListener{
    fun onItemRemoved(item: LocationItem)
}