package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

// ==================== STATE ====================

data class EditorState(
    val image: ImageBitmap? = null,
    val imageUri: String = "",
    val projectId: String? = null,

    val selectedTool: EditorTool = EditorTool.SELECT,
    val cropRect: Rect? = null,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val appliedFilters: List<FilterType> = emptyList(),

    val detectedFaces: List<FaceRect> = emptyList(),
    val isDetectingFaces: Boolean = false,

    val drawingPaths: List<DrawingPath> = emptyList(),
    val brushSize: Float = 10f,
    val brushColor: Color = Color.Black,

    val historyStack: List<EditorSnapshot> = emptyList(),
    val currentHistoryIndex: Int = -1,

    val isProcessing: Boolean = false,
    val isSaving: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val showToolPanel: Boolean = true,
    val error: String? = null
)

enum class EditorTool {
    SELECT,
    CROP,
    FILTER,
    ADJUST,
    DRAW,
    FACE_CUT,
    ERASER
}

enum class FilterType {
    NONE,
    GRAYSCALE,
    SEPIA,
    VINTAGE,
    WARM,
    COOL,
    HIGH_CONTRAST
}

data class FaceRect(
    val rect: Rect,
    val confidence: Float
)

data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

data class EditorSnapshot(
    val timestamp: Long,
    val cropRect: Rect?,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val appliedFilters: List<FilterType>,
    val drawingPaths: List<DrawingPath>,
    val detectedFaces: List<FaceRect>
)

// ==================== INTENT ====================

sealed interface EditorIntent {
    data class SelectTool(val tool: EditorTool) : EditorIntent
    data object ToggleToolPanel : EditorIntent

    data class UpdateCropRect(val rect: Rect) : EditorIntent
    data object ApplyCrop : EditorIntent
    data object CancelCrop : EditorIntent

    data class ApplyFilter(val filter: FilterType) : EditorIntent
    data class RemoveFilter(val filter: FilterType) : EditorIntent

    data class UpdateBrightness(val value: Float) : EditorIntent
    data class UpdateContrast(val value: Float) : EditorIntent
    data class UpdateSaturation(val value: Float) : EditorIntent

    data class StartDrawing(val point: Offset) : EditorIntent
    data class ContinueDrawing(val point: Offset) : EditorIntent
    data object EndDrawing : EditorIntent
    data class ChangeBrushSize(val size: Float) : EditorIntent
    data class ChangeBrushColor(val color: Color) : EditorIntent

    data object DetectFaces : EditorIntent
    data class SelectFace(val faceIndex: Int) : EditorIntent
    data object CutFaces : EditorIntent

    data object Undo : EditorIntent
    data object Redo : EditorIntent
    data object ResetToOriginal : EditorIntent

    data object Save : EditorIntent
    data object Export : EditorIntent
    data object Share : EditorIntent

    data object DismissError : EditorIntent
}

// ==================== EFFECT ====================

sealed interface EditorEffect {
    data class ShowSnackbar(val message: String) : EditorEffect
    data class ShowError(val message: String) : EditorEffect
    data class NavigateToProcessing(val projectId: String, val operation: String) : EditorEffect
    data object NavigateBack : EditorEffect
    data class ShareImage(val uri: String) : EditorEffect
}