package org.aditya1875.facenox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.aditya1875.facenox.di.appModule
import org.aditya1875.facenox.di.platformAppModule
import org.koin.core.context.GlobalContext.startKoin

fun main() = application {
    startKoin {
        modules(
            platformAppModule,
            appModule
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "FaceNox"
    ) {
        App()
    }
}

