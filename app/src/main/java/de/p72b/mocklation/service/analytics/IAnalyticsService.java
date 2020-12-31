package de.p72b.mocklation.service.analytics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IAnalyticsService {
    void trackEvent(@NonNull String name);

    void trackEvent(@NonNull String name, @Nullable Bundle bundle);

    void setAnalyticsCollectionEnabled(Boolean enabled);
}
