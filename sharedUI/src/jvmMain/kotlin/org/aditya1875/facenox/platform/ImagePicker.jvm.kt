package org.aditya1875.facenox.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame

class DesktopImagePicker : ImagePicker {
    override fun pickImage(onResult: (String?) -> Unit) {
        val dialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name ->
            val lower = name.lowercase()
            lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".bmp") || lower.endsWith(".gif") || lower.endsWith(".webp")
        }
        dialog.isVisible = true
        val file = dialog.file
        onResult(if (file != null) "${dialog.directory}$file" else null)
    }
}

@Composable
actual fun rememberImagePicker(): ImagePicker = remember {
    DesktopImagePicker()
}
