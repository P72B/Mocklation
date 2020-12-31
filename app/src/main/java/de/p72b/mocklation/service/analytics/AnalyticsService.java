package de.p72b.mocklation.service.analytics;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AnalyticsService implements IAnalyticsService {

    public static class Event {
        public static final String ADD_FAVORITE = "add_favorite";
        public static final String REMOVE_FAVORITE = "remove_favorite";
        public static final String PAUSE_MOCK_LOCATION_SERVICE = "pause_mock_location_service";
        public static final String STOP_MOCK_LOCATION_SERVICE = "stop_mock_location_service";
        public static final String START_MOCK_LOCATION_SERVICE = "start_mock_location_service";
        public static final String DISTANCE_BETWEEN_USER_LOCATION_AND_MOCK = "distance_between_user_location_and_mock";

        protected Event() {
        }
    }

    private FirebaseAnalytics mFirebaseAnalytics;

    public AnalyticsService(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void trackEvent(@NonNull String name) {
        trackEvent(name, null);
    }

    @Override
    public void trackEvent(@NonNull String name, @Nullable Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        mFirebaseAnalytics.logEvent(name, bundle);
    }

    @Override
    public void setAnalyticsCollectionEnabled(Boolean enabled) {
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
    }
}
