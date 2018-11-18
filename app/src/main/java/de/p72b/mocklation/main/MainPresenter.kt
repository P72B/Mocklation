package de.p72b.mocklation.main

import android.app.Activity
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import de.p72b.mocklation.BuildConfig
import de.p72b.mocklation.R
import de.p72b.mocklation.dialog.PrivacyUpdateDialog
import de.p72b.mocklation.imprint.ImprintActivity
import de.p72b.mocklation.main.mode.fixed.FixedFragment
import de.p72b.mocklation.main.mode.route.RouteFragment
import de.p72b.mocklation.map.MapsActivity
import de.p72b.mocklation.service.setting.ISetting
import de.p72b.mocklation.util.Constants

class MainPresenter(private val activity: Activity, private val view: MainActivity,
                    private val setting: ISetting) {

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private var fixedFragment: FixedFragment = FixedFragment()
    private var routeFragment: RouteFragment = RouteFragment()

    init {
        setupRemoteConfig()
    }


    private fun setupRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        firebaseRemoteConfig.setConfigSettings(configSettings)
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)

        fetchRemoteConfig()
    }


    private fun fetchRemoteConfig() {
        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        if (firebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }
        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activateFetched()
                    }
                }
    }

    fun onStart() {
        view.show(fixedFragment)
    }

    fun onNavigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.vNavFixedMode -> view.show(fixedFragment)
            R.id.vNavRouteMode -> view.show(routeFragment)
            R.id.vNavImprint -> activity.startActivity(Intent(activity, ImprintActivity::class.java))
        }
    }

    fun onClick(id: Int) {
        when (id) {
            R.id.vFab -> {
                if (!setting.isPrivacyStatementAccepted) {
                    showPrivacyUpdateDialog()
                    return
                }
                activity.startActivity(Intent(activity, MapsActivity::class.java))
            }
        }
    }

    private fun showPrivacyUpdateDialog() {
        val fragmentManager = view.supportFragmentManager
        val dialog = PrivacyUpdateDialog.newInstance(
                object : PrivacyUpdateDialog.PrivacyUpdateDialogListener {
                    override fun onAcceptClick() {
                        setting.acceptCurrentPrivacyStatement()
                        activity.startActivity(Intent(activity, MapsActivity::class.java))
                    }

                    override fun onDeclineClick() {
                        view.showSnackbar(R.string.error_1020, -1, null,
                                Snackbar.LENGTH_LONG)
                    }
                }, firebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_KEY_URL_PRIVACY_POLICY))
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme)
        dialog.show(fragmentManager, PrivacyUpdateDialog.TAG)
    }
}