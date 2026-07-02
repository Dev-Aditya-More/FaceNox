package org.aditya1875.facenox.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class FaceNoxThemeConfig(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isAmoled: Boolean = false,
    val isMaterialYou: Boolean = true,
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    @Serializable(with = ColorSerializer::class)
    val seedColor: Color = DefaultSeedColor
)

@Serializable
enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
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

object ColorSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeLong(value.value.toLong())
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color(decoder.decodeLong().toULong())
    }
}

@Composable
expect fun FaceNoxTheme(
    themeConfig: FaceNoxThemeConfig = FaceNoxThemeConfig(),
    content: @Composable () -> Unit
)

@Composable
expect fun isSystemInDarkTheme(): Boolean
