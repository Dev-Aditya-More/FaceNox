package org.aditya1875.facenox.feature.screens.imageselection

import androidx.compose.runtime.Composable

expect interface ImagePicker {
    fun pickImage(onResult: (String?) -> Unit)
}

@Composable
expect fun rememberImagePicker(): ImagePicker
