package org.aditya1875.facenox.platform

import androidx.compose.ui.graphics.ImageBitmap

interface ImageLoader {
    suspend fun loadImage(uri: String): ImageBitmap?
}