package org.aditya1875.facenox

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.aditya1875.facenox.core.navigation.FaceNoxNavGraph
import org.aditya1875.facenox.theme.AppTheme
import org.aditya1875.facenox.theme.FaceNoxTheme
import org.aditya1875.facenox.theme.FaceNoxThemeConfig
import org.aditya1875.facenox.theme.PaletteStyle

@Composable
fun App() {
    FaceNoxTheme(
        themeConfig = FaceNoxThemeConfig(
            appTheme = AppTheme.DARK,
            isAmoled = true,
            isMaterialYou = false,
            seedColor = Color(0xFFE8B67E),
            paletteStyle = PaletteStyle.Expressive
        )
    ) {
        FaceNoxNavGraph()
    }
}