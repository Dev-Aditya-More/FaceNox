package org.aditya1875.facenox.platform

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import org.aditya1875.facenox.feature.screens.editor.EditorState

// jvmMain
class DesktopImageProcessor : ImageProcessor {
    override suspend fun crop(image: ImageBitmap, rect: Rect): ImageBitmap {
        // TODO later
        return image
    }

    override suspend fun processAndSave(
        image: ImageBitmap,
        edits: EditorState
    ): String {
        TODO("Not yet implemented")
    }
}
