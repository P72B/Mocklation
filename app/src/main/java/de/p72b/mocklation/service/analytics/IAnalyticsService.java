package de.p72b.mocklation.service.analytics;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface IAnalyticsService {
    void trackEvent(@NonNull String name);

    void trackEvent(@NonNull String name, @Nullable Bundle bundle);
}
