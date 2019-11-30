package de.p72b.mocklation.revamp.view.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.p72b.mocklation.App
import de.p72b.mocklation.R
import de.p72b.mocklation.revamp.arch.LocationViewModel
import kotlinx.android.synthetic.main.fragment_tracks.*

class TracksFragment : Fragment() {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var adapter: ListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tracks, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)

        initAdapter()

        locationViewModel.items.observe(viewLifecycleOwner, Observer { if (it != null) {
            adapter.setData(it)
        } })
    }

    private fun initAdapter() {
        adapter =ListAdapter(App.sInstance)
        vRecyclerView.adapter = adapter
        vRecyclerView.layoutManager = LinearLayoutManager(App.sInstance)
    }
}