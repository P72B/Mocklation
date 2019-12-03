package de.p72b.mocklation.revamp.view.tracks

import android.view.View
import com.google.android.material.card.MaterialCardView
import de.p72b.mocklation.revamp.room.LocationItem
import de.p72b.mocklation.util.Logger

class Handler {
    fun onCardClick(card: View, item: LocationItem) {
        Logger.d("p72b", "${item.code}")
        card as MaterialCardView
        card.isChecked = !card.isChecked
    }
}