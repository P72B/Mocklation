package de.p72b.mocklation.settings

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.p72b.mocklation.R
import de.p72b.mocklation.service.AppServices
import de.p72b.mocklation.service.analytics.IAnalyticsService

class DataProtectionSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    var analytics = AppServices.getService(AppServices.ANALYTICS) as IAnalyticsService

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.data_protection, rootKey)

        findPreference<Preference>("link_tnc")?.let { tncButtonPreferences ->
            tncButtonPreferences.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    if (preference == null) return false

                    context?.let {
                        CustomTabsIntent.Builder().build().launchUrl(it, Uri.parse(TNC_URL))
                    }
                    return true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null) return
        val currentValue = sharedPreferences.getBoolean(key, false)
        when (key) {
            getString(R.string.pref_key_use_analytics) -> {
                analytics.setAnalyticsCollectionEnabled(currentValue)
            }
        }
    }

    companion object {
        private const val TNC_URL = "https://sites.google.com/view/p72b/startseite"
    }
}