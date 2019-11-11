package de.p72b.mocklation.revamp.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.p72b.mocklation.R
import de.p72b.mocklation.revamp.main.player.PlayerFragment
import de.p72b.mocklation.revamp.main.recorder.RecorderFragment
import de.p72b.mocklation.revamp.main.tracks.TracksFragment

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    var playerFragment = PlayerFragment()
    var tracksFragment = TracksFragment()
    var recorderFragment = RecorderFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(playerFragment)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.vNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        when (item.itemId) {
            R.id.navigation_player -> fragment = playerFragment
            R.id.navigation_tracks -> fragment = tracksFragment
            R.id.navigation_recorder -> fragment = recorderFragment
        }
        return loadFragment(fragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // TODO: startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        //switching fragment
        if (fragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.vFragmentContainer, fragment)
                    .commit()
            return true
        }
        return false
    }
}