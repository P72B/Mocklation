package de.p72b.mocklation.main.mode

import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import de.p72b.mocklation.R
import de.p72b.mocklation.service.room.LocationItem
import de.p72b.mocklation.util.Logger
import de.p72b.mocklation.util.VisibilityAnimationListener


abstract class BaseModeFragment : Fragment(), View.OnClickListener {

    private lateinit var rootView: View
    private lateinit var dataView: View
    private lateinit var dataEmpty: View
    private lateinit var fadeInAnimation: Animation
    private lateinit var fadeOutAnimation: Animation
    private lateinit var recyclerView: RecyclerView
    private lateinit var presenter: BaseModePresenter
    private lateinit var buttonPlayStop: ImageButton
    private lateinit var buttonPausePlay: ImageButton
    private lateinit var favorite: ImageButton
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
        initBaseView()
        initServiceItemBar()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter = provideBaseModePresenter()
        activity?.let{
            presenter.setActivity(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            MockServiceInteractor.PERMISSIONS_MOCKING -> presenter.onMockPermissionsResult(grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
    override fun onClick(view: View?) {
        if (view == null) {
            return
        }
        presenter.onClick(view.id)
    }

    fun showEmptyPlaceholder() {
        toggleDataViewTo(View.INVISIBLE)
    }

    fun showSavedLocations(locationItems: List<LocationItem>) {
        toggleDataViewTo(View.VISIBLE)
        adapter.setData(locationItems)
    }

    fun showSnackbar(message: Int, action: Int, listener: View.OnClickListener?, duration: Int) {
        val snackbar = Snackbar.make(rootView, message, duration)
        if (action != -1) {
            snackbar.setAction(action, listener)
        }
        snackbar.show()
    }

    fun setPlayPauseStopStatus(state: Int) {
        when (state) {
            MockServiceInteractor.SERVICE_STATE_RUNNING -> {
                buttonPlayStop.setBackgroundResource(R.drawable.ic_stop_black_24dp)
                buttonPausePlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
                buttonPausePlay.visibility = View.VISIBLE
            }
            MockServiceInteractor.SERVICE_STATE_STOP -> {
                buttonPlayStop.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
                buttonPausePlay.visibility = View.INVISIBLE
            }
            MockServiceInteractor.SERVICE_STATE_PAUSE -> buttonPausePlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        }
    }

    private fun initBaseView() {
        dataView = rootView.findViewById(R.id.vDataView)
        dataEmpty = rootView.findViewById(R.id.vDataEmpty)
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

        if (activity == null) {
            return
        }
        val toolbar = activity!!.findViewById<Toolbar>(R.id.vToolbar)
        val root = activity!!.findViewById<View>(R.id.vMainRoot)
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val rectangle = Rect()
                activity!!.window.decorView.getWindowVisibleDisplayFrame(rectangle)
                val statusBarHeight = rectangle.top
                val rootHeight = root.height
                val selectedLocationCardHeight = rootView.findViewById<View>(R.id.vCardViewSelectedLocation).height
                val toolbarHeight = toolbar.height
                val padding15dp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        15f, resources.displayMetrics))

                val newHeight = rootHeight - statusBarHeight - toolbarHeight - selectedLocationCardHeight - 4 * padding15dp

                val params = recyclerView.layoutParams
                params.height = newHeight
                recyclerView.layoutParams = params
            }
        })
    }

    private fun initServiceItemBar() {
        buttonPlayStop = rootView.findViewById(R.id.vPlayStop)
        buttonPlayStop.setOnClickListener(this)

        buttonPausePlay = rootView.findViewById(R.id.vPause)
        buttonPausePlay.setOnClickListener(this)

        favorite = rootView.findViewById(R.id.vFavorite)
        favorite.setOnClickListener(this)

        rootView.findViewById<View>(R.id.vEdit).setOnClickListener(this)
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
        favorite.background = activity!!.getDrawable(if (item.isIsFavorite)
            R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)

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