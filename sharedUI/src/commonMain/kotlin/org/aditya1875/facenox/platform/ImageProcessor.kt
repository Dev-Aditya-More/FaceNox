package org.aditya1875.facenox.platform

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import org.aditya1875.facenox.feature.screens.editor.EditorState

// commonMain
interface ImageProcessor {
    suspend fun crop(
        image: ImageBitmap,
        rect: Rect
    ): ImageBitmap

    suspend fun processAndSave(
        image: ImageBitmap,
        edits: EditorState
    ): String
}
