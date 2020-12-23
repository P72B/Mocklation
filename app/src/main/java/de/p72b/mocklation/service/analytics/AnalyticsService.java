package de.p72b.mocklation.service.analytics;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.p72b.mocklation.App;

public class AnalyticsService implements IAnalyticsService {

    public static class Event {
        @Deprecated
        public static final String ADD_FAVORITE = "add_favorite";
        @Deprecated
        public static final String REMOVE_FAVORITE = "remove_favorite";
        public static final String PAUSE_MOCK_LOCATION_SERVICE = "pause_mock_location_service";
        public static final String STOP_MOCK_LOCATION_SERVICE = "stop_mock_location_service";
        public static final String START_MOCK_LOCATION_SERVICE = "start_mock_location_service";

        protected Event() {
        }
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
        FirebaseAnalytics.getInstance(App.sInstance).logEvent(name, bundle);
    }
}
