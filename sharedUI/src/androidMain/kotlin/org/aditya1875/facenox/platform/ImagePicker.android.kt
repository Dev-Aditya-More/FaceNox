package org.aditya1875.facenox.platform

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class AndroidImagePicker(
    private val activity: ComponentActivity
) : ImagePicker {

    private var callback: ((String?) -> Unit)? = null

    private val galleryLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            callback?.invoke(uri?.toString())
        }

    override fun pickImage(onResult: (String?) -> Unit) {
        callback = onResult
        galleryLauncher.launch("image/*")
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker {
    var callback by remember { mutableStateOf<((String?) -> Unit)?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        callback?.invoke(uri?.toString())
    }

    return remember {
        object : ImagePicker {
            override fun pickImage(onResult: (String?) -> Unit) {
                callback = onResult
                galleryLauncher.launch("image/*")
            }
        }
    }
}
