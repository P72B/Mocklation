package de.p72b.mocklation

import android.annotation.SuppressLint
import android.app.Application
import com.crashlytics.android.Crashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import de.p72b.mocklation.util.Logger
import io.fabric.sdk.android.Fabric
import com.google.android.gms.tasks.OnCompleteListener
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var sInstance: App
    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this

        Fabric.with(this, Crashlytics())
        initLogging()
        initFirebase()

        //(AppServices.getService(AppServices.ANALYTICS) as IAnalyticsService).trackEvent(FirebaseAnalytics.Event.APP_OPEN)
    }

    private fun initFirebase() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(getMinimumFetchInterval())
                .build()
        FirebaseRemoteConfig.getInstance().setConfigSettingsAsync(configSettings)
        FirebaseRemoteConfig.getInstance().setDefaultsAsync(R.xml.remote_config_defaults)
        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        val numCores = Runtime.getRuntime().availableProcessors()
        val executor = ThreadPoolExecutor(numCores * 2, numCores * 2,
                60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())
        FirebaseRemoteConfig.getInstance().fetch(getMinimumFetchInterval())
                .addOnCompleteListener(executor, OnCompleteListener { result ->
                    run {
                        if (result.isSuccessful) {
                            FirebaseRemoteConfig.getInstance().activate()
                        }
                    }
                })
    }

    /**
     * Returns cache expiration time. For debug builds it's 0 minutes and for release builds it's
     * a hour.
     */
    private fun getMinimumFetchInterval(): Long {
        return if (BuildConfig.DEBUG) 0L else 3_600L
    }

    private fun initLogging() {
        Logger.enableLogging(BuildConfig.BUILD_TYPE !== "release")
    }
}