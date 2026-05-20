package org.aditya1875.facenox

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.aditya1875.facenox.di.appModule
import org.aditya1875.facenox.di.platformAppModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun main() {
    startKoin {
        modules(platformAppModule, appModule)
    }

    application {
        Window(
            onCloseRequest = {
                stopKoin()
                exitApplication()
            },
            title = "FaceNox",
            state = rememberWindowState(width = 1280.dp, height = 800.dp)
        ) {
            App()
        }
    }
}
