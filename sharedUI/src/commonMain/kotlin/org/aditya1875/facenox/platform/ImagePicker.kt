package org.aditya1875.facenox.platform

import androidx.compose.runtime.Composable

interface ImagePicker {
    fun pickImage(onResult: (String?) -> Unit)
}
// commonMain
@Composable
expect fun rememberImagePicker(): ImagePicker
