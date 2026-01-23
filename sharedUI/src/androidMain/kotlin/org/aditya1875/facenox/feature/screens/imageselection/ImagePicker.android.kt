package org.aditya1875.facenox.feature.screens.imageselection

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

actual interface ImagePicker {
    actual fun pickImage(onResult: (String?) -> Unit)
}

@Composable
actual fun rememberImagePicker(): ImagePicker {

    var currentCallback: ((String?) -> Unit)? by remember { mutableStateOf(null) }
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            currentCallback?.invoke(uri?.toString())
        }

    return remember {
        object : ImagePicker {
            override fun pickImage(onResult: (String?) -> Unit) {
                currentCallback = onResult
                launcher.launch("image/*")
            }
        }
    }
}