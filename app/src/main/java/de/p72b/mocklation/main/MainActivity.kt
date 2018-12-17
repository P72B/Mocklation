package de.p72b.mocklation.main

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import de.p72b.locator.location.LocationAwareAppCompatActivity
import de.p72b.mocklation.BuildConfig
import de.p72b.mocklation.R
import de.p72b.mocklation.main.mode.fixed.FixedFragment
import de.p72b.mocklation.main.mode.route.RouteFragment
import de.p72b.mocklation.service.AppServices
import de.p72b.mocklation.service.setting.ISetting


class MainActivity : LocationAwareAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fabAddNewLocationItem: FloatingActionButton
    private lateinit var toolbarLayout: View
    private lateinit var presenter: MainPresenter
    private var actionBarTitle: TextView? = null
    private var colorLeft = 0
    private var colorFixedFragment = 0
    private var colorRouteFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val setting = AppServices.getService(AppServices.SETTINGS) as ISetting
        presenter = MainPresenter(this, this, setting)
        val toolbar = findViewById<Toolbar>(R.id.vToolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.vNavView)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.vNavFixedMode)
        (findViewById<View>(R.id.vNavFooterItem) as TextView).text = BuildConfig.VERSION_NAME
        toolbarLayout = findViewById(R.id.vToolbarLayout)
        val actionBar = supportActionBar
        actionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar?.setCustomView(R.layout.custom_action_bar)
        actionBarTitle = actionBar?.customView!!.findViewById(R.id.vCustomActionBarTitle)

        fabAddNewLocationItem = findViewById(R.id.vFab)
        fabAddNewLocationItem.setOnClickListener(this)

        colorFixedFragment = ContextCompat.getColor(this, R.color.colorPrimary)
        colorRouteFragment = ContextCompat.getColor(this, R.color.r1)
        colorLeft = colorFixedFragment
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        presenter.onNavigationItemSelected(item.itemId)

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onClick(view: View?) {
        if (view == null) {
            return
        }
        presenter.onClick(view.id)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager?.fragments.let { fragmentList ->
            fragmentList?.forEach {
                it.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    fun show(fragment: Fragment) {
        updateAppBar(fragment)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.vMainContainer, fragment)
        fragmentTransaction.commit()
    }

    fun showSnackbar(message: Int, action: Int, listener: View.OnClickListener?, duration: Int) {
        val snackbar = Snackbar.make(findViewById(R.id.vMainRoot), message, duration)
        if (action != -1) {
            snackbar.setAction(action, listener)
        }
        snackbar.show()
    }

    private fun updateAppBar(fragment: Fragment) {
        var title = ""
        var color = colorFixedFragment
        when (fragment) {
            is FixedFragment -> {
                title = getString(R.string.title_activity_fixed_mode)
                color = colorFixedFragment
            }
            is RouteFragment -> {
                title = getString(R.string.title_activity_route_mode)
                color = colorRouteFragment
            }
        }
        val colorArray = arrayOf(ColorDrawable(colorLeft), ColorDrawable(color))
        val transitionDrawable = TransitionDrawable(colorArray)

        actionBarTitle?.text = title

        toolbarLayout.background = transitionDrawable
        transitionDrawable.startTransition(1500)

        colorLeft = color
    }
}