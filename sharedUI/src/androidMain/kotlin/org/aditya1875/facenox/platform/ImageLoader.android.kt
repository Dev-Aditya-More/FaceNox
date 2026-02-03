package org.aditya1875.facenox.platform

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri

class AndroidImageLoader(private val context: Context) : ImageLoader {
    override suspend fun loadImage(uri: String): ImageBitmap? {
        return try {
            val contentUri = uri.toUri()
            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun provideImageLoader(context: Context): ImageLoader {
    return AndroidImageLoader(context)
}