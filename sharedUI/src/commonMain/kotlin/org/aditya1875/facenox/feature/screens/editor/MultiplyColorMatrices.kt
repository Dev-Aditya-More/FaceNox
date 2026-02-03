package org.aditya1875.facenox.feature.screens.editor

fun multiplyColorMatrices(a: FloatArray, b: FloatArray): FloatArray {
    val out = FloatArray(20)

    for (row in 0..3) {
        for (col in 0..4) {
            out[row * 5 + col] =
                a[row * 5 + 0] * b[col + 0] +
                        a[row * 5 + 1] * b[col + 5] +
                        a[row * 5 + 2] * b[col + 10] +
                        a[row * 5 + 3] * b[col + 15] +
                        if (col == 4) a[row * 5 + 4] else 0f
        }
    }
    return out
}
