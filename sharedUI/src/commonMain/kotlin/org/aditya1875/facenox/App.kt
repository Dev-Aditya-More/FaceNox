package org.aditya1875.facenox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.aditya1875.facenox.core.navigation.FaceNoxNavGraph
import org.aditya1875.facenox.theme.FaceNoxTheme
import org.aditya1875.facenox.theme.ThemeStateHolder
import org.koin.compose.koinInject

@Composable
fun App() {
    val themeStateHolder = koinInject<ThemeStateHolder>()
    val themeConfig by themeStateHolder.themeConfig.collectAsState()

    FaceNoxTheme(themeConfig = themeConfig) {
        FaceNoxNavGraph()
    }
}
