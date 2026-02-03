package org.aditya1875.facenox.platform

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.aditya1875.facenox.feature.screens.editor.EditorState
import java.io.File

class AndroidImageProcessor(private val context: Context) : ImageProcessor {

    override suspend fun crop(
        image: ImageBitmap,
        rect: Rect
    ): ImageBitmap {
        val src = image.asAndroidBitmap()

        val cropped = Bitmap.createBitmap(
            src,
            rect.left.toInt().coerceAtLeast(0),
            rect.top.toInt().coerceAtLeast(0),
            rect.width.toInt().coerceAtMost(src.width),
            rect.height.toInt().coerceAtMost(src.height)
        )

        return cropped.asImageBitmap()
    }

    override suspend fun processAndSave(
        image: ImageBitmap,
        edits: EditorState
    ): String {
        val file = File(
            context.cacheDir,
            "facenox_${System.currentTimeMillis()}.png"
        )

        return file.toURI().toString()
    }
}
