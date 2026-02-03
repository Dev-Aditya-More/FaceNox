package org.aditya1875.facenox.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame

class DesktopImagePicker : ImagePicker {
    override fun pickImage(onResult: (String?) -> Unit) {
        val dialog = FileDialog(null as Frame?, "Pick Image")
        dialog.isVisible = true
        val file = dialog.file ?: return
        onResult("${dialog.directory}$file")
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker = remember {
    DesktopImagePicker()
}
