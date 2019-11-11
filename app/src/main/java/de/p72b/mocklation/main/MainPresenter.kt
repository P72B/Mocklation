package de.p72b.mocklation.main

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import de.p72b.mocklation.R
import de.p72b.mocklation.dialog.PrivacyUpdateDialog
import de.p72b.mocklation.imprint.ImprintActivity
import de.p72b.mocklation.main.mode.fixed.FixedFragment
import de.p72b.mocklation.main.mode.route.RouteFragment
import de.p72b.mocklation.map.MapsActivity
import de.p72b.mocklation.service.setting.ISetting
import de.p72b.mocklation.util.Constants

class MainPresenter(private val activity: Activity, private val view: OldMainActivity,
                    private val setting: ISetting) {

    private var fixedFragment: FixedFragment = FixedFragment()
    private var routeFragment: RouteFragment = RouteFragment()

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
                }, FirebaseRemoteConfig.getInstance().getString(Constants.REMOTE_CONFIG_KEY_URL_PRIVACY_POLICY))
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme)
        dialog.show(fragmentManager, PrivacyUpdateDialog.TAG)
    }
}