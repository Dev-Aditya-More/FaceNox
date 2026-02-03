package org.aditya1875.facenox.androidApp

import android.app.Application
import org.aditya1875.facenox.di.appModule
import org.aditya1875.facenox.di.platformAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class FaceNoxApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FaceNoxApplication)
            modules(
                appModule,
                platformAppModule
            )
        }
    }
}
