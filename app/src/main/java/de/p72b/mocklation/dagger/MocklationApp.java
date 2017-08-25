package de.p72b.mocklation.dagger;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;


public final class MocklationApp extends Application {
    @SuppressLint("StaticFieldLeak")
    @NonNull
    private static MocklationApp sInstance;
    private MocklationComponent mainComponent;

    @Override public void onCreate() {
        super.onCreate();
        sInstance = this;

        mainComponent = DaggerMocklationComponent.builder().mocklationModule(new MocklationModule(this)).build();
    }

    public static MocklationApp getInstance() {
        return sInstance;
    }

    public static MocklationComponent getComponent(Context context) {
        return ((MocklationApp) context.getApplicationContext()).mainComponent;
    }
}
