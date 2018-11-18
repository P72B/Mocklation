package de.p72b.mocklation.main

import android.app.Activity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import de.p72b.mocklation.BuildConfig
import de.p72b.mocklation.R

class MainerPresenter(private val activity: Activity) {
    
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

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
}