package org.aditya1875.facenox.feature.screens.imageselection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual interface ImagePicker {
    actual fun pickImage(onResult: (String?) -> Unit)
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    return remember {
        object : ImagePicker {
            override fun pickImage(onResult: (String?) -> Unit) {
                onResult(null) // TODO later
            }
        }
    }
}