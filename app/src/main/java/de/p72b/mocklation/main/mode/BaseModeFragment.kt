package de.p72b.mocklation.main.mode

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import de.p72b.mocklation.R
import de.p72b.mocklation.service.room.LocationItem
import de.p72b.mocklation.util.VisibilityAnimationListener


abstract class BaseModeFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var dataView: View
    private lateinit var dataEmpty: View
    private lateinit var fadeInAnimation: Animation
    private lateinit var fadeOutAnimation: Animation
    private lateinit var recyclerView: RecyclerView
    private lateinit var presenter: BaseModePresenter
    private var adapter = LocationListAdapter(AdapterListener())
    private val fadeOutListener = VisibilityAnimationListener()
    private val fadeInListener = VisibilityAnimationListener()

    abstract fun provideBaseFragmentView(inflater: LayoutInflater, parent: ViewGroup?,
                                         savedInstanceState: Bundle?): View

    abstract fun provideBaseModePresenter(): BaseModePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in_animation)
        fadeInAnimation.setAnimationListener(fadeInListener)
        fadeOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out_animation)
        fadeOutAnimation.setAnimationListener(fadeOutListener)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanseState: Bundle?): View? {
        rootView = provideBaseFragmentView(inflater, parent, savedInstanseState)
        presenter = provideBaseModePresenter()
        initBaseView()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    fun showEmptyPlaceholder() {
        toggleDataViewTo(View.INVISIBLE)
    }

    fun showSavedLocations(locationItems: List<LocationItem>) {
        toggleDataViewTo(View.VISIBLE)
        adapter.setData(locationItems)
    }

    private fun initBaseView() {
        dataView = rootView.findViewById<View>(R.id.vDataView)
        dataEmpty = rootView.findViewById<View>(R.id.vDataEmpty)
        dataView.visibility = View.INVISIBLE
        dataEmpty.visibility = View.INVISIBLE

        recyclerView = rootView.findViewById(R.id.vLocationList)
        recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(SwipeAndTouchHelper(adapter))
        touchHelper.attachToRecyclerView(recyclerView)
    }

    private fun toggleDataViewTo(state: Int) {
        if (View.INVISIBLE == state) {
            if (dataEmpty.visibility != View.VISIBLE) {
                fadeInListener.setViewAndVisibility(dataEmpty, View.VISIBLE)
                dataEmpty.startAnimation(fadeInAnimation)
            }
            if (dataView.visibility != View.INVISIBLE) {
                fadeOutListener.setViewAndVisibility(dataView, View.INVISIBLE)
                dataView.startAnimation(fadeOutAnimation)
            }
        } else {
            if (dataEmpty.visibility != View.INVISIBLE) {
                fadeOutListener.setViewAndVisibility(dataEmpty, View.INVISIBLE)
                dataEmpty.startAnimation(fadeOutAnimation)
            }
            if (dataView.visibility != View.VISIBLE) {
                fadeInListener.setViewAndVisibility(dataView, View.VISIBLE)
                dataView.startAnimation(fadeInAnimation)
            }
        }
    }

    fun selectLocation(item: LocationItem) {
        adapter.flagItem(item)
    }

    private inner class AdapterListener : IAdapterListener {

        override fun onClick(view: View) {
            val position = recyclerView.getChildLayoutPosition(view)
            val item = adapter.getItemAt(position)
            presenter.locationItemPressed(item)
        }

        override fun onItemRemoved(item: LocationItem) {
            presenter.locationItemRemoved(item)
        }
    }
}