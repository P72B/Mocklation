package de.p72b.mocklation.service;

import androidx.annotation.StringDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import de.p72b.mocklation.service.analytics.AnalyticsService;
import de.p72b.mocklation.service.setting.Setting;

public final class AppServices {

    public static final String SETTINGS = "SETTINGS";
    public static final String ANALYTICS = "ANALYTICS";

    private final static Map<String, Object> SERVICES = new HashMap<>();

    static {
        SERVICES.put(SETTINGS, new Setting());
        SERVICES.put(ANALYTICS, new AnalyticsService());
    }

    public AppServices() {
        // do nothing hide this
    }

    public static Object getService(@Service String service) {
        return SERVICES.get(service);
    }

    @StringDef({SETTINGS, ANALYTICS})
    @Retention(RetentionPolicy.SOURCE)
    @interface Service {
    }
}
