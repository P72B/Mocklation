package de.p72b.mocklation.revamp.ui.tracks

import android.view.View
import com.google.android.material.card.MaterialCardView
import de.p72b.mocklation.revamp.arch.LocationViewModel
import de.p72b.mocklation.revamp.room.LocationItem

class Handler(private val viewModel: LocationViewModel) {

    fun onCardClick(card: View, item: LocationItem) {
        card as MaterialCardView
        val newState = !card.isChecked
        card.isChecked = newState
        viewModel.changeSelection(item, newState)
    }
}