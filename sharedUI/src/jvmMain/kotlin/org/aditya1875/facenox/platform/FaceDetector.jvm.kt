package org.aditya1875.facenox.platform

import androidx.compose.ui.graphics.ImageBitmap
import org.aditya1875.facenox.feature.screens.editor.FaceRect

class DesktopFaceDetector : FaceDetector {
    override val isSupported = false
    override suspend fun detect(image: ImageBitmap): List<FaceRect> = emptyList()
}
