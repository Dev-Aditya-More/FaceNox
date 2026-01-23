package org.aditya1875.facenox.feature.screens.imageselection

import androidx.compose.ui.graphics.ImageBitmap

data class ImageSelectionState(
    val selectedImageUri: String? = null,
    val previewImage: ImageBitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageSource: ImageSource? = null
)

enum class ImageSource {
    GALLERY,
    CAMERA,
    FILE
}
