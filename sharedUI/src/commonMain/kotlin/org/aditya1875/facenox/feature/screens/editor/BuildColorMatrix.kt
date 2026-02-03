package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.ui.graphics.ColorMatrix

fun buildColorMatrix(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    filter: List<FilterType>
): ColorMatrix {

    val b = brightness * 255f
    val c = contrast + 1f
    val t = (1f - c) * 128f
    val s = saturation + 1f

    val lumR = 0.3086f
    val lumG = 0.6094f
    val lumB = 0.0820f

    val sr = (1f - s) * lumR
    val sg = (1f - s) * lumG
    val sb = (1f - s) * lumB

    var result = floatArrayOf(
        c * (sr + s), c * sg,       c * sb,       0f, b + t,
        c * sr,       c * (sg + s), c * sb,       0f, b + t,
        c * sr,       c * sg,       c * (sb + s), 0f, b + t,
        0f,           0f,           0f,           1f, 0f
    )

    filter.forEach { filterType ->
        val filterMatrix = filterToMatrix(filterType).values
        result = multiplyColorMatrices(result, filterMatrix)
    }

    return ColorMatrix(result)
}

fun filterToMatrix(filter: FilterType): ColorMatrix =
    when (filter) {
        FilterType.GRAYSCALE -> ColorMatrix().apply {
            setToSaturation(0f)
        }

        FilterType.SEPIA -> ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        FilterType.VINTAGE -> ColorMatrix(
            floatArrayOf(
                0.6f, 0.3f, 0.1f, 0f, 30f,
                0.2f, 0.7f, 0.1f, 0f, 30f,
                0.2f, 0.1f, 0.6f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        FilterType.WARM -> ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, 20f,
                0f, 1.1f, 0f, 0f, 10f,
                0f, 0f, 0.9f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        FilterType.COOL -> ColorMatrix(
            floatArrayOf(
                0.9f, 0f, 0f, 0f, 0f,
                0f, 1.0f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        FilterType.HIGH_CONTRAST -> ColorMatrix(
            floatArrayOf(
                1.5f, 0f, 0f, 0f, -50f,
                0f, 1.5f, 0f, 0f, -50f,
                0f, 0f, 1.5f, 0f, -50f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        FilterType.NONE -> ColorMatrix()
    }