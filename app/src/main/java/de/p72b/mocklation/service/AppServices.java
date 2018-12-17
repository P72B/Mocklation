package de.p72b.mocklation.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.StringDef;
import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.service.analytics.AnalyticsService;
import de.p72b.mocklation.service.setting.Setting;

public final class AppServices {

    private static final String WEB_SERVICE = "WEB_SERVICE";
    public static final String SETTINGS = "SETTINGS";
    public static final String ANALYTICS = "ANALYTICS";

    private final static Map<String, Object> SERVICES = new HashMap<>();

    static {
        SERVICES.put(SETTINGS, new Setting(MocklationApp.getInstance()));
        SERVICES.put(ANALYTICS, new AnalyticsService(MocklationApp.getInstance()));
    }

    public AppServices() {
        // do nothing hide this
    }

    public static Object getService(@Service String service) {
        return SERVICES.get(service);
    }

    @StringDef({WEB_SERVICE, SETTINGS, ANALYTICS})
    @Retention(RetentionPolicy.SOURCE)
    @interface Service {
    }
}
