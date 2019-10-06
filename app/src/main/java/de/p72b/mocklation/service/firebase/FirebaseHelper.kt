package de.p72b.mocklation.service.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import de.p72b.mocklation.BuildConfig

object FirebaseHelper {

    private const val CONFIG_PRODUCTION_JSON = "CONFIG_PRODUCTION_JSON"
    private const val CONFIG_INTEGRATION_JSON = "CONFIG_INTEGRATION_JSON"

    private val appConfig: AppRemoteConfig by lazy {
        val key = if(BuildConfig.DEBUG) CONFIG_INTEGRATION_JSON else CONFIG_PRODUCTION_JSON
        val configString = FirebaseRemoteConfig.getInstance().getString(key)
        Gson().fromJson(configString, AppRemoteConfig::class.java) ?: AppRemoteConfig()
    }

    val appCredits: AppCredits
        get() = appConfig.appCredits
}