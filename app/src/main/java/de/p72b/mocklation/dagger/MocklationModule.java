package de.p72b.mocklation.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.p72b.mocklation.service.database.DbModule;

@Module(
        includes = {
                DbModule.class,
        }
)
public final class MocklationModule {
    private final Application mApplication;

    MocklationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }
}