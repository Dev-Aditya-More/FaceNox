package org.aditya1875.facenox.theme


import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle as MaterialKolorPaletteStyle

@Composable
actual fun FaceNoxTheme(
    themeConfig: FaceNoxThemeConfig,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isDark = when (themeConfig.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val useMaterialYou = themeConfig.isMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    if (useMaterialYou) {
        val colorScheme = if (isDark) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }

        val finalColorScheme = if (themeConfig.isAmoled && isDark) {
            colorScheme.copy(
                background = androidx.compose.ui.graphics.Color.Black,
                surface = androidx.compose.ui.graphics.Color.Black
            )
        } else {
            colorScheme
        }

        MaterialTheme(
            colorScheme = finalColorScheme,
            content = content
        )
    } else {
        DynamicMaterialTheme(
            seedColor = themeConfig.seedColor,
            isDark = isDark,
            isAmoled = themeConfig.isAmoled,
            style = themeConfig.paletteStyle.toMaterialKolorStyle(),
            content = content
        )
    }
}

@Composable
actual fun isSystemInDarkTheme(): Boolean = isSystemInDarkTheme()

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