package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

enum class EditorTool {
    SELECT, CROP, FILTER, ADJUST, DRAW, BACKGROUND_REMOVE, FACE_EDIT, ERASER
}

enum class FilterType {
    NONE, GRAYSCALE, SEPIA, VINTAGE, WARM, COOL, HIGH_CONTRAST
}

data class EditorState(
    // Images
    val originalImage: ImageBitmap? = null,
    val currentImage: ImageBitmap? = null,
    val imageUri: String = "",
    val projectId: String? = null,

    // Tools
    val selectedTool: EditorTool = EditorTool.SELECT,
    val showToolPanel: Boolean = true,

    val isEditingMode: Boolean = false,

    // Crop — rect is in *image pixel* coordinates
    val cropRect: Rect? = null,

    // Adjustments
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,

    // Filters
    val appliedFilters: List<FilterType> = emptyList(),

    // Drawing
    val drawingPaths: List<DrawingPath> = emptyList(),
    val brushSize: Float = 10f,
    val brushColor: Color = Color.Black,

    // Background removal
    val hasBackgroundRemoved: Boolean = false,

    // Face detection
    val detectedFaces: List<FaceRect> = emptyList(),
    val isDetectingFaces: Boolean = false,

    val historyStack: List<EditorSnapshot> = emptyList(),
    val currentHistoryIndex: Int = -1,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,

    // UI
    val isProcessing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

data class FaceRect(
    val rect: Rect,
    val confidence: Float,
    val smilingProbability: Float? = null,
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null
)

data class EditorSnapshot(
    val timestamp: Long,
    val image: ImageBitmap,
    val cropRect: Rect?,
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val appliedFilters: List<FilterType>,
    val drawingPaths: List<DrawingPath>,
    val detectedFaces: List<FaceRect>,
    val hasBackgroundRemoved: Boolean
)

sealed class EditorIntent {
    data class SelectTool(val tool: EditorTool) : EditorIntent()
    data object ToggleToolPanel : EditorIntent()
    data object CloseToolOptions : EditorIntent()
    data object EnterEditingMode : EditorIntent()
    data object ExitEditingMode : EditorIntent()

    data class UpdateCropRect(val rect: Rect) : EditorIntent()
    data object ApplyCrop : EditorIntent()
    data object CancelCrop : EditorIntent()

    data class ApplyFilter(val filter: FilterType) : EditorIntent()
    data class RemoveFilter(val filter: FilterType) : EditorIntent()

    data class UpdateBrightness(val value: Float) : EditorIntent()
    data class UpdateContrast(val value: Float) : EditorIntent()
    data class UpdateSaturation(val value: Float) : EditorIntent()

    data class ChangeBrushSize(val size: Float) : EditorIntent()
    data class ChangeBrushColor(val color: Color) : EditorIntent()
    data class StartDrawing(val point: Offset) : EditorIntent()
    data class ContinueDrawing(val point: Offset) : EditorIntent()
    data object EndDrawing : EditorIntent()

    data object RotateClockwise : EditorIntent()
    data object RotateCounterClockwise : EditorIntent()

    data object RemoveBackground : EditorIntent()
    data object RestoreBackground : EditorIntent()

    data object DetectFaces : EditorIntent()
    data class SelectFace(val faceIndex: Int) : EditorIntent()
    data object CutFaces : EditorIntent()
    data object BlurAllFaces : EditorIntent()
    data class CropToFace(val faceIndex: Int) : EditorIntent()

    data object Undo : EditorIntent()
    data object Redo : EditorIntent()
    data object ResetToOriginal : EditorIntent()

    data object Save : EditorIntent()
    data object Export : EditorIntent()
    data object Share : EditorIntent()

    data object DismissError : EditorIntent()
}

sealed class EditorEffect {
    data class ShowSnackbar(val message: String) : EditorEffect()
    data class ShowError(val message: String) : EditorEffect()
    data class NavigateToProcessing(
        val projectId: String,
        val operation: org.aditya1875.facenox.core.navigation.ProcessingOperation
    ) : EditorEffect()
    data object NavigateBack : EditorEffect()
    data class ShareImage(val uri: String) : EditorEffect()
}