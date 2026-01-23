package org.aditya1875.facenox.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class FaceNoxThemeConfig(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isAmoled: Boolean = false,
    val isMaterialYou: Boolean = true,
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val seedColor: Color = DefaultSeedColor
)

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class PaletteStyle {
    TonalSpot,
    Neutral,
    Vibrant,
    Expressive,
    Rainbow,
    FruitSalad,
    Monochrome,
    Fidelity,
    Content
}

val DefaultSeedColor = Color(0xFF7FA5B8)

@Composable
expect fun FaceNoxTheme(
    themeConfig: FaceNoxThemeConfig = FaceNoxThemeConfig(),
    content: @Composable () -> Unit
)

@Composable
expect fun isSystemInDarkTheme(): Boolean