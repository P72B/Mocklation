package de.p72b.mocklation

import android.app.Application
import de.p72b.mocklation.di.appModule
import de.p72b.mocklation.parser.di.parserModule
import de.p72b.mocklation.util.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MocklationApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.setEnabled(BuildConfig.BUILD_TYPE !== "release")

        startKoin {
            androidContext(this@MocklationApplication)
            androidLogger()
            modules(listOf(appModule, parserModule))
        }
    }
}