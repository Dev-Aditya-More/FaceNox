package org.aditya1875.facenox.platform

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.aditya1875.facenox.feature.screens.editor.EditorState
import org.aditya1875.facenox.feature.screens.editor.buildColorMatrix
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.Surface
import java.io.File

class DesktopImageProcessor : ImageProcessor {

    override suspend fun crop(image: ImageBitmap, rect: Rect): ImageBitmap {
        val src = Image.makeFromBitmap(image.asSkiaBitmap())
        val x = rect.left.toInt().coerceIn(0, src.width - 1)
        val y = rect.top.toInt().coerceIn(0, src.height - 1)
        val w = rect.width.toInt().coerceIn(1, src.width - x)
        val h = rect.height.toInt().coerceIn(1, src.height - y)
        val surface = Surface.makeRasterN32Premul(w, h)
        surface.canvas.drawImage(src, -x.toFloat(), -y.toFloat())
        return surface.makeImageSnapshot().toComposeImageBitmap()
    }

    override suspend fun rotate(image: ImageBitmap, degrees: Int): ImageBitmap {
        val src = Image.makeFromBitmap(image.asSkiaBitmap())
        val norm = ((degrees % 360) + 360) % 360
        val (newW, newH) = if (norm == 90 || norm == 270) src.height to src.width else src.width to src.height
        val surface = Surface.makeRasterN32Premul(newW, newH)
        val canvas = surface.canvas
        canvas.translate(newW / 2f, newH / 2f)
        canvas.rotate(degrees.toFloat())
        canvas.translate(-src.width / 2f, -src.height / 2f)
        canvas.drawImage(src, 0f, 0f)
        return surface.makeImageSnapshot().toComposeImageBitmap()
    }

    override suspend fun blurRegion(image: ImageBitmap, rect: Rect): ImageBitmap {
        val src = Image.makeFromBitmap(image.asSkiaBitmap())
        val x = rect.left.toInt().coerceIn(0, src.width - 1)
        val y = rect.top.toInt().coerceIn(0, src.height - 1)
        val w = rect.width.toInt().coerceIn(1, src.width - x)
        val h = rect.height.toInt().coerceIn(1, src.height - y)

        val faceSurface = Surface.makeRasterN32Premul(w, h)
        faceSurface.canvas.drawImage(src, -x.toFloat(), -y.toFloat())
        val faceImg = faceSurface.makeImageSnapshot()

        val pixelSize = 15
        val downW = (w / pixelSize).coerceAtLeast(1)
        val downH = (h / pixelSize).coerceAtLeast(1)

        val downSurface = Surface.makeRasterN32Premul(downW, downH)
        downSurface.canvas.drawImageRect(
            faceImg,
            org.jetbrains.skia.Rect.makeWH(w.toFloat(), h.toFloat()),
            org.jetbrains.skia.Rect.makeWH(downW.toFloat(), downH.toFloat())
        )
        val downImg = downSurface.makeImageSnapshot()

        val result = Surface.makeRasterN32Premul(src.width, src.height)
        result.canvas.drawImage(src, 0f, 0f)
        result.canvas.drawImageRect(
            downImg,
            org.jetbrains.skia.Rect.makeWH(downW.toFloat(), downH.toFloat()),
            org.jetbrains.skia.Rect.makeXYWH(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat())
        )
        return result.makeImageSnapshot().toComposeImageBitmap()
    }

    override suspend fun processAndSave(image: ImageBitmap, edits: EditorState): String {
        val composeMatrix = buildColorMatrix(
            brightness = edits.brightness,
            contrast = edits.contrast,
            saturation = edits.saturation,
            filter = edits.appliedFilters
        )
        val v = composeMatrix.values
        val skiaColorFilter = ColorFilter.makeMatrix(
            ColorMatrix(
                v[0],  v[1],  v[2],  v[3],  v[4],
                v[5],  v[6],  v[7],  v[8],  v[9],
                v[10], v[11], v[12], v[13], v[14],
                v[15], v[16], v[17], v[18], v[19]
            )
        )
        val src = Image.makeFromBitmap(image.asSkiaBitmap())
        val surface = Surface.makeRasterN32Premul(src.width, src.height)
        val canvas = surface.canvas

        canvas.drawImage(src, 0f, 0f, Paint().apply { colorFilter = skiaColorFilter })

        if (edits.drawingPaths.isNotEmpty()) {
            val drawPaint = Paint().apply {
                strokeCap = PaintStrokeCap.ROUND
                mode = PaintMode.STROKE
                isAntiAlias = true
            }
            edits.drawingPaths.forEach { path ->
                if (path.points.size < 2) return@forEach
                val c = path.color
                drawPaint.color = ((c.alpha * 255).toInt().coerceIn(0, 255) shl 24) or
                        ((c.red * 255).toInt().coerceIn(0, 255) shl 16) or
                        ((c.green * 255).toInt().coerceIn(0, 255) shl 8) or
                        (c.blue * 255).toInt().coerceIn(0, 255)
                drawPaint.strokeWidth = path.strokeWidth
                for (i in 0 until path.points.size - 1) {
                    canvas.drawLine(path.points[i].x, path.points[i].y, path.points[i + 1].x, path.points[i + 1].y, drawPaint)
                }
            }
        }

        val dir = File(System.getProperty("user.home"), "Pictures${File.separator}FaceNox")
        dir.mkdirs()
        val file = File(dir, "facenox_${System.currentTimeMillis()}.png")
        val data = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)
            ?: throw Exception("Failed to encode image to PNG")
        file.writeBytes(data.bytes)
        return "file://${file.absolutePath}"
    }
}
