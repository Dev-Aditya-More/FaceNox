package org.aditya1875.facenox.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.io.File

class DesktopImageLoader : ImageLoader {
    override suspend fun loadImage(uri: String): ImageBitmap? {
        return try {
            val file = File(uri)
            if (!file.exists()) return null

            val bytes = file.readBytes()
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun provideImageLoader() : ImageLoader {
    return DesktopImageLoader()
}