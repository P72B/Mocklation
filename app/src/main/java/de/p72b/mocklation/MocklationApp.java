package de.p72b.mocklation;

import android.annotation.SuppressLint;
import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.analytics.IAnalyticsService;
import de.p72b.mocklation.util.Logger;

public final class MocklationApp
        extends Application {
    @SuppressLint("StaticFieldLeak")
    private static MocklationApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        Logger.enableLogging(BuildConfig.BUILD_TYPE != "release");
        ((IAnalyticsService) AppServices.getService(AppServices.ANALYTICS)).trackEvent(FirebaseAnalytics.Event.APP_OPEN);
    }

    public static MocklationApp getInstance() {
        return sInstance;
    }
}
