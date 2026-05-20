package org.aditya1875.facenox.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.aditya1875.facenox.feature.screens.editor.EditorState
import org.aditya1875.facenox.feature.screens.editor.buildColorMatrix
import java.io.File

class AndroidImageProcessor(private val context: Context) : ImageProcessor {

    override suspend fun crop(image: ImageBitmap, rect: Rect): ImageBitmap {
        val src = image.asAndroidBitmap()
        val x = rect.left.toInt().coerceAtLeast(0)
        val y = rect.top.toInt().coerceAtLeast(0)
        val w = rect.width.toInt().coerceAtMost(src.width - x).coerceAtLeast(1)
        val h = rect.height.toInt().coerceAtMost(src.height - y).coerceAtLeast(1)
        return Bitmap.createBitmap(src, x, y, w, h).asImageBitmap()
    }

    override suspend fun rotate(image: ImageBitmap, degrees: Int): ImageBitmap {
        val src = image.asAndroidBitmap()
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true).asImageBitmap()
    }

    override suspend fun blurRegion(image: ImageBitmap, rect: Rect): ImageBitmap {
        val src = image.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val x = rect.left.toInt().coerceAtLeast(0)
        val y = rect.top.toInt().coerceAtLeast(0)
        val w = rect.width.toInt().coerceAtMost(src.width - x).coerceAtLeast(1)
        val h = rect.height.toInt().coerceAtMost(src.height - y).coerceAtLeast(1)

        val region = Bitmap.createBitmap(src, x, y, w, h)
        val pixelSize = 15
        val down = Bitmap.createScaledBitmap(region, (w / pixelSize).coerceAtLeast(1), (h / pixelSize).coerceAtLeast(1), false)
        val pixelated = Bitmap.createScaledBitmap(down, w, h, false)

        Canvas(src).drawBitmap(pixelated, x.toFloat(), y.toFloat(), null)
        return src.asImageBitmap()
    }

    override suspend fun processAndSave(image: ImageBitmap, edits: EditorState): String {
        val src = image.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)

        val composeMatrix = buildColorMatrix(
            brightness = edits.brightness,
            contrast = edits.contrast,
            saturation = edits.saturation,
            filter = edits.appliedFilters
        )
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(android.graphics.ColorMatrix(composeMatrix.values))
        })

        if (edits.drawingPaths.isNotEmpty()) {
            val drawPaint = Paint().apply {
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            edits.drawingPaths.forEach { path ->
                if (path.points.size < 2) return@forEach
                val c = path.color
                drawPaint.color = Color.argb(
                    (c.alpha * 255).toInt().coerceIn(0, 255),
                    (c.red * 255).toInt().coerceIn(0, 255),
                    (c.green * 255).toInt().coerceIn(0, 255),
                    (c.blue * 255).toInt().coerceIn(0, 255)
                )
                drawPaint.strokeWidth = path.strokeWidth
                val androidPath = Path()
                androidPath.moveTo(path.points[0].x, path.points[0].y)
                for (i in 1 until path.points.size) androidPath.lineTo(path.points[i].x, path.points[i].y)
                canvas.drawPath(androidPath, drawPaint)
            }
        }

        val dir = File(context.filesDir, "projects")
        dir.mkdirs()
        val file = File(dir, "facenox_${System.currentTimeMillis()}.png")
        file.outputStream().use { stream ->
            result.compress(Bitmap.CompressFormat.PNG, 95, stream)
        }
        return "file://${file.absolutePath}"
    }
}
