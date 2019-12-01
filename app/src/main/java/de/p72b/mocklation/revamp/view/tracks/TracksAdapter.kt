package de.p72b.mocklation.revamp.view.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import de.p72b.mocklation.R
import de.p72b.mocklation.databinding.RecyclerviewLocationItemBinding
import de.p72b.mocklation.revamp.room.LocationItem

class ListAdapter: RecyclerView.Adapter<ListAdapter.RepositoryViewHolder>() {

    private var list: List<LocationItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RecyclerviewLocationItemBinding>(
                layoutInflater, R.layout.recyclerview_location_item, parent, false)
        return RepositoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val item = list[position]
        holder.binding.item = item
    }

    fun setData(items: List<LocationItem>) {
        list = items
        notifyDataSetChanged()
    }

    inner class RepositoryViewHolder(val binding: RecyclerviewLocationItemBinding) : RecyclerView.ViewHolder(binding.root)
}