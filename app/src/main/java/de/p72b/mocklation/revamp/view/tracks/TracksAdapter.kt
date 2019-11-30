package de.p72b.mocklation.revamp.view.tracks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.p72b.mocklation.R
import de.p72b.mocklation.revamp.room.LocationItem

class ListAdapter(context: Context) : RecyclerView.Adapter<ListAdapter.RepositoryViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private var list: List<LocationItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val viewItem = inflater.inflate(R.layout.recyclerview_location_item, parent, false)
        return RepositoryViewHolder(
            viewItem
        )
    }

    override fun getItemCount(): Int {
        return when (list) {
            null -> 0
            else -> list!!.size
        }
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val item = list!![position]
        holder.root.setOnClickListener {
            //listener.onRepositoryClicked(item)
        }
        holder.name.text = "${item.title} geom:${item.geom}"
    }

    fun setData(items: List<LocationItem>) {
        list = items
        notifyDataSetChanged()
    }

    class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root: View = itemView.findViewById(R.id.vItemRoot)
        val name: TextView = itemView.findViewById(R.id.vName)
    }
}