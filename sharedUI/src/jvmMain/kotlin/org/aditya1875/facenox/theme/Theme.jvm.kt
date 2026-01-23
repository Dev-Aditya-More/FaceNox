package org.aditya1875.facenox.theme

import androidx.compose.runtime.Composable
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle as MaterialKolorPaletteStyle

@Composable
actual fun FaceNoxTheme(
    themeConfig: FaceNoxThemeConfig,
    content: @Composable () -> Unit
) {
    val isDark = when (themeConfig.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    DynamicMaterialTheme(
        seedColor = themeConfig.seedColor,
        isDark = isDark,
        isAmoled = themeConfig.isAmoled,
        style = themeConfig.paletteStyle.toMaterialKolorStyle(),
        content = content
    )
}

@Composable
actual fun isSystemInDarkTheme(): Boolean {
    // Desktop: Default to light theme
    // You can integrate with system settings if needed
    return false
}

private fun PaletteStyle.toMaterialKolorStyle(): MaterialKolorPaletteStyle {
    return when (this) {
        PaletteStyle.TonalSpot -> MaterialKolorPaletteStyle.TonalSpot
        PaletteStyle.Neutral -> MaterialKolorPaletteStyle.Neutral
        PaletteStyle.Vibrant -> MaterialKolorPaletteStyle.Vibrant
        PaletteStyle.Expressive -> MaterialKolorPaletteStyle.Expressive
        PaletteStyle.Rainbow -> MaterialKolorPaletteStyle.Rainbow
        PaletteStyle.FruitSalad -> MaterialKolorPaletteStyle.FruitSalad
        PaletteStyle.Monochrome -> MaterialKolorPaletteStyle.Monochrome
        PaletteStyle.Fidelity -> MaterialKolorPaletteStyle.Fidelity
        PaletteStyle.Content -> MaterialKolorPaletteStyle.Content
    }
}