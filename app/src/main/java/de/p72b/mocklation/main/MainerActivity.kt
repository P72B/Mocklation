package de.p72b.mocklation.main

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.support.design.widget.NavigationView
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
import de.p72b.mocklation.BuildConfig
import de.p72b.mocklation.R
import de.p72b.mocklation.imprint.ImprintActivity
import de.p72b.mocklation.main.mode.fixed.FixedFragment
import de.p72b.mocklation.main.mode.route.RouteFragment


class MainerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fixedFragment: FixedFragment
    private lateinit var routeFragment: RouteFragment
    private lateinit var toolbarLayout: View
    private var actionBarTitle: TextView? = null
    private var colorLeft = 0
    private var colorFixedFragment = 0
    private var colorRouteFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        actionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        actionBar?.setCustomView(R.layout.custom_action_bar)
        actionBarTitle = actionBar?.customView!!.findViewById(R.id.vCustomActionBarTitle)


        fixedFragment = FixedFragment()
        routeFragment = RouteFragment()

        colorFixedFragment = ContextCompat.getColor(this, R.color.colorPrimary)
        colorRouteFragment = ContextCompat.getColor(this, R.color.r1)
        colorLeft = colorFixedFragment

        show(fixedFragment)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.vNavFixedMode -> show(fixedFragment)
            R.id.vNavRouteMode -> show(routeFragment)
            R.id.vNavImprint -> startActivity(Intent(this, ImprintActivity::class.java))
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun show(fragment: Fragment) {
        updateAppBar(fragment)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.vMainContainer, fragment)
        fragmentTransaction.commit()
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

        toolbarLayout.setBackground(transitionDrawable)
        transitionDrawable.startTransition(1500)

        colorLeft = color
    }
}