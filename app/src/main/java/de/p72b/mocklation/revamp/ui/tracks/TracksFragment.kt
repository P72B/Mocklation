package de.p72b.mocklation.revamp.ui.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.p72b.mocklation.App
import de.p72b.mocklation.R
import de.p72b.mocklation.databinding.FragmentTracksBinding
import de.p72b.mocklation.revamp.arch.LocationViewModel
import de.p72b.mocklation.revamp.util.SwipeAndTouchHelper

class TracksFragment : Fragment() {

    private lateinit var binding: FragmentTracksBinding
    private lateinit var viewModel: LocationViewModel
    private lateinit var adapter: TracksAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        adapter = TracksAdapter(viewModel)

        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_tracks,
                container,
                false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.vRecyclerView.adapter = adapter
        binding.vRecyclerView.layoutManager = LinearLayoutManager(App.sInstance)

        viewModel.all.observe(this, Observer { items ->
            if (items != null) {
                adapter.setData(items)
            }
            binding.invalidateAll()
        })

        viewModel.lastDeletedItem.observe(this, Observer {
            showSnackbar(R.string.message_location_item_removed, R.string.snackbar_action_undo,
                    View.OnClickListener { viewModel.undo() })
        })

        val touchHelper = ItemTouchHelper(SwipeAndTouchHelper(adapter))
        touchHelper.attachToRecyclerView(binding.vRecyclerView)

        return binding.root
    }

    private fun showSnackbar(message: Int,
                             action: Int = -1,
                             listener: View.OnClickListener? = null,
                             duration: Int = Snackbar.LENGTH_LONG) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        if (action != -1) {
            snackbar.setAction(action, listener)
        }
        snackbar.show()
    }
}