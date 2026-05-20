package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aditya1875.facenox.core.navigation.ProcessingOperation
import org.aditya1875.facenox.platform.FaceDetector
import org.aditya1875.facenox.platform.ImageLoader
import org.aditya1875.facenox.platform.ImageProcessor

class EditorViewModel(
    private val projectId: String?,
    private val imageUri: String,
    private val imageLoader: ImageLoader,
    private val imageProcessor: ImageProcessor,
    private val faceDetector: FaceDetector
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState(imageUri = imageUri, projectId = projectId))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditorEffect>()
    val effect: SharedFlow<EditorEffect> = _effect.asSharedFlow()

    val isFaceDetectionSupported: Boolean get() = faceDetector.isSupported

    private var currentDrawingPath: MutableList<Offset>? = null

    init { loadImage() }

    fun onIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.SelectTool,
            is EditorIntent.ToggleToolPanel,
            is EditorIntent.CloseToolOptions,
            is EditorIntent.UpdateCropRect,
            is EditorIntent.ApplyFilter,
            is EditorIntent.RemoveFilter,
            is EditorIntent.UpdateBrightness,
            is EditorIntent.UpdateContrast,
            is EditorIntent.UpdateSaturation,
            is EditorIntent.ChangeBrushSize,
            is EditorIntent.ChangeBrushColor,
            is EditorIntent.Undo,
            is EditorIntent.Redo,
            is EditorIntent.ResetToOriginal,
            is EditorIntent.DismissError -> reduceState(intent)

            is EditorIntent.ApplyCrop -> handleApplyCrop()
            is EditorIntent.CancelCrop -> handleCancelCrop()
            is EditorIntent.RotateClockwise -> handleRotate(90)
            is EditorIntent.RotateCounterClockwise -> handleRotate(-90)
            is EditorIntent.StartDrawing -> handleStartDrawing(intent.point)
            is EditorIntent.ContinueDrawing -> handleContinueDrawing(intent.point)
            is EditorIntent.EndDrawing -> handleEndDrawing()
            is EditorIntent.DetectFaces -> handleDetectFaces()
            is EditorIntent.BlurAllFaces -> handleBlurAllFaces()
            is EditorIntent.CropToFace -> handleCropToFace(intent.faceIndex)
            is EditorIntent.SelectFace -> handleSelectFace(intent.faceIndex)
            is EditorIntent.CutFaces -> handleCutFaces()
            is EditorIntent.Save -> handleSave()
            is EditorIntent.Export -> handleExport()
            is EditorIntent.Share -> handleShare()
            else -> {}
        }
    }

    private fun reduceState(intent: EditorIntent) {
        _state.update { EditorReducer.reduce(it, intent) }
    }

    // ── Image loading ──────────────────────────────────────────────────────

    private fun loadImage() {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            val bitmap = imageLoader.loadImage(imageUri)
            if (bitmap != null) {
                _state.update {
                    EditorReducer.addToHistory(it.copy(originalImage = bitmap, currentImage = bitmap, isProcessing = false))
                }
            } else {
                _state.update { it.copy(isProcessing = false) }
                _effect.emit(EditorEffect.ShowError("Failed to load image"))
            }
        }
    }

    // ── Crop ──────────────────────────────────────────────────────────────

    private fun handleApplyCrop() {
        viewModelScope.launch {
            val image = _state.value.currentImage ?: return@launch
            val rect = _state.value.cropRect ?: return@launch
            if (rect.width < 1f || rect.height < 1f) {
                _state.update { it.copy(cropRect = null, isEditingMode = false, selectedTool = EditorTool.SELECT) }
                return@launch
            }
            _state.update { it.copy(isProcessing = true) }
            val cropped = imageProcessor.crop(image = image, rect = rect)
            _state.update { current ->
                EditorReducer.addToHistory(current.copy(
                    currentImage = cropped,
                    cropRect = null,
                    isEditingMode = false,
                    selectedTool = EditorTool.SELECT,
                    isProcessing = false
                ))
            }
        }
    }

    private fun handleCancelCrop() {
        _state.update { it.copy(cropRect = null, isEditingMode = false, selectedTool = EditorTool.SELECT) }
    }

    // ── Rotate ────────────────────────────────────────────────────────────

    private fun handleRotate(degrees: Int) {
        viewModelScope.launch {
            val image = _state.value.currentImage ?: return@launch
            _state.update { it.copy(isProcessing = true) }
            val rotated = imageProcessor.rotate(image, degrees)
            _state.update { current ->
                EditorReducer.addToHistory(current.copy(
                    currentImage = rotated,
                    // Re-seed crop rect to rotated image bounds so crop overlay stays visible
                    cropRect = if (current.isEditingMode) {
                        androidx.compose.ui.geometry.Rect(0f, 0f, rotated.width.toFloat(), rotated.height.toFloat())
                    } else null,
                    isProcessing = false
                ))
            }
        }
    }

    fun createCropRect(imageWidth: Float, imageHeight: Float, ratio: Float): Rect {
        return if (imageWidth / imageHeight > ratio) {
            val targetHeight = imageHeight
            val targetWidth = imageHeight * ratio
            val left = (imageWidth - targetWidth) / 2f
            Rect(left, 0f, left + targetWidth, targetHeight)
        } else {
            val targetWidth = imageWidth
            val targetHeight = imageWidth / ratio
            val top = (imageHeight - targetHeight) / 2f
            Rect(0f, top, targetWidth, top + targetHeight)
        }
    }

    // ── Drawing ───────────────────────────────────────────────────────────

    private fun handleStartDrawing(point: Offset) { currentDrawingPath = mutableListOf(point) }
    private fun handleContinueDrawing(point: Offset) { currentDrawingPath?.add(point) }

    private fun handleEndDrawing() {
        val path = currentDrawingPath?.takeIf { it.size > 1 } ?: run { currentDrawingPath = null; return }
        _state.update { current ->
            EditorReducer.addToHistory(current.copy(
                drawingPaths = current.drawingPaths + DrawingPath(
                    points = path.toList(),
                    color = current.brushColor,
                    strokeWidth = current.brushSize
                )
            ))
        }
        currentDrawingPath = null
    }

    // ── Face detection ────────────────────────────────────────────────────

    private fun handleDetectFaces() {
        viewModelScope.launch {
            val image = _state.value.currentImage ?: return@launch
            _state.update { it.copy(isDetectingFaces = true) }
            try {
                val faces = faceDetector.detect(image)
                _state.update { it.copy(detectedFaces = faces, isDetectingFaces = false) }
                if (faces.isEmpty()) {
                    _effect.emit(EditorEffect.ShowSnackbar("No faces detected"))
                } else {
                    _effect.emit(EditorEffect.ShowSnackbar("${faces.size} face(s) detected"))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isDetectingFaces = false, error = e.message ?: "Face detection failed") }
            }
        }
    }

    private fun handleBlurAllFaces() {
        viewModelScope.launch {
            val faces = _state.value.detectedFaces
            if (faces.isEmpty()) return@launch
            _state.update { it.copy(isProcessing = true) }
            var image = _state.value.currentImage ?: return@launch
            faces.forEach { face -> image = imageProcessor.blurRegion(image, face.rect) }
            _state.update { current ->
                EditorReducer.addToHistory(current.copy(currentImage = image, isProcessing = false))
            }
            _effect.emit(EditorEffect.ShowSnackbar("${faces.size} face(s) blurred"))
        }
    }

    private fun handleCropToFace(faceIndex: Int) {
        val face = _state.value.detectedFaces.getOrNull(faceIndex) ?: return
        val image = _state.value.currentImage ?: return
        val padding = (face.rect.width * 0.3f).coerceAtLeast(30f)
        val paddedRect = Rect(
            left = (face.rect.left - padding).coerceAtLeast(0f),
            top = (face.rect.top - padding).coerceAtLeast(0f),
            right = (face.rect.right + padding).coerceAtMost(image.width.toFloat()),
            bottom = (face.rect.bottom + padding).coerceAtMost(image.height.toFloat())
        )
        _state.update { it.copy(cropRect = paddedRect) }
        handleApplyCrop()
    }

    private fun handleSelectFace(faceIndex: Int) {
        val face = _state.value.detectedFaces.getOrNull(faceIndex) ?: return
        _state.update { it.copy(cropRect = face.rect, selectedTool = EditorTool.CROP, isEditingMode = true) }
    }

    private fun handleCutFaces() {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            _state.update { it.copy(isProcessing = false) }
            _effect.emit(EditorEffect.ShowSnackbar("Face cut — coming soon"))
        }
    }

    // ── Save / Export / Share ─────────────────────────────────────────────

    private fun handleSave() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val snapshot = EditorReducer.createSnapshot(_state.value)
                val id = projectId ?: "project_${snapshot.timestamp}"
                EditorSessionStore.save(id, snapshot)
                _state.update { it.copy(isSaving = false) }
                _effect.emit(EditorEffect.NavigateToProcessing(projectId = id, operation = ProcessingOperation.SAVE))
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Save failed") }
            }
        }
    }

    private fun handleExport() {
        viewModelScope.launch {
            val id = projectId ?: "export_${System.currentTimeMillis()}"
            _effect.emit(EditorEffect.NavigateToProcessing(id, ProcessingOperation.EXPORT))
        }
    }

    private fun handleShare() {
        viewModelScope.launch {
            _effect.emit(EditorEffect.ShareImage("content://share/image.png"))
        }
    }
}
