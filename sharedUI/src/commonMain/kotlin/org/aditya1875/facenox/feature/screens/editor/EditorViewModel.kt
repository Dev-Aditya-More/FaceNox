package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditorViewModel(
    private val projectId: String?,
    private val imageUri: String
) : ViewModel() {

    private val _state = MutableStateFlow(
        EditorState(
            imageUri = imageUri,
            projectId = projectId
        )
    )
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditorEffect>()
    val effect: SharedFlow<EditorEffect> = _effect.asSharedFlow()

    private var currentDrawingPath: MutableList<Offset>? = null

    init {
        loadImage()
    }

    fun onIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.SelectTool,
            is EditorIntent.ToggleToolPanel,
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
            is EditorIntent.DismissError -> {
                reduceState(intent)
            }

            is EditorIntent.ApplyCrop -> handleApplyCrop()
            is EditorIntent.CancelCrop -> handleCancelCrop()
            is EditorIntent.StartDrawing -> handleStartDrawing(intent.point)
            is EditorIntent.ContinueDrawing -> handleContinueDrawing(intent.point)
            is EditorIntent.EndDrawing -> handleEndDrawing()
            is EditorIntent.DetectFaces -> handleDetectFaces()
            is EditorIntent.SelectFace -> handleSelectFace(intent.faceIndex)
            is EditorIntent.CutFaces -> handleCutFaces()
            is EditorIntent.Save -> handleSave()
            is EditorIntent.Export -> handleExport()
            is EditorIntent.Share -> handleShare()
        }
    }

    private fun reduceState(intent: EditorIntent) {
        _state.update { currentState ->
            EditorReducer.reduce(currentState, intent)
        }
    }

    private fun loadImage() {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            try {
                // TODO: Load image
                _state.update { it.copy(isProcessing = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "Failed to load image"
                    )
                }
            }
        }
    }

    private fun handleApplyCrop() {
        viewModelScope.launch {
            val cropRect = _state.value.cropRect ?: return@launch

            _state.update { it.copy(isProcessing = true) }

            try {
                // TODO: Apply crop

                _state.update {
                    EditorReducer.reduce(
                        it.copy(
                            cropRect = null,
                            isProcessing = false,
                            selectedTool = EditorTool.SELECT
                        ),
                        EditorIntent.SelectTool(EditorTool.SELECT)
                    )
                }

                _effect.emit(EditorEffect.ShowSnackbar("Crop applied"))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "Failed to apply crop"
                    )
                }
            }
        }
    }

    private fun handleCancelCrop() {
        _state.update {
            it.copy(
                cropRect = null,
                selectedTool = EditorTool.SELECT
            )
        }
    }

    private fun handleStartDrawing(point: Offset) {
        currentDrawingPath = mutableListOf(point)
    }

    private fun handleContinueDrawing(point: Offset) {
        currentDrawingPath?.add(point)
    }

    private fun handleEndDrawing() {
        val path = currentDrawingPath
        if (path != null && path.size > 1) {
            val drawingPath = DrawingPath(
                points = path.toList(),
                color = _state.value.brushColor,
                strokeWidth = _state.value.brushSize
            )

            _state.update {
                EditorReducer.reduce(
                    it.copy(drawingPaths = it.drawingPaths + drawingPath),
                    EditorIntent.SelectTool(it.selectedTool)
                )
            }
        }
        currentDrawingPath = null
    }

    private fun handleDetectFaces() {
        viewModelScope.launch {
            _state.update { it.copy(isDetectingFaces = true) }

            try {
                // TODO: Detect faces
                val mockFaces = emptyList<FaceRect>()

                _state.update {
                    it.copy(
                        detectedFaces = mockFaces,
                        isDetectingFaces = false
                    )
                }

                if (mockFaces.isEmpty()) {
                    _effect.emit(EditorEffect.ShowSnackbar("No faces detected"))
                } else {
                    _effect.emit(EditorEffect.ShowSnackbar("${mockFaces.size} face(s) detected"))
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isDetectingFaces = false,
                        error = e.message ?: "Failed to detect faces"
                    )
                }
            }
        }
    }

    private fun handleSelectFace(faceIndex: Int) {
        val faces = _state.value.detectedFaces
        if (faceIndex in faces.indices) {
            val face = faces[faceIndex]
            _state.update {
                it.copy(
                    cropRect = face.rect,
                    selectedTool = EditorTool.CROP
                )
            }
        }
    }

    private fun handleCutFaces() {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            try {
                // TODO: Cut faces
                _state.update { it.copy(isProcessing = false) }
                _effect.emit(EditorEffect.ShowSnackbar("Faces cut successfully"))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "Failed to cut faces"
                    )
                }
            }
        }
    }

    private fun handleSave() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            try {
                val savedProjectId = projectId ?: "project_${System.currentTimeMillis()}"
                _state.update { it.copy(isSaving = false) }
                _effect.emit(EditorEffect.NavigateToProcessing(savedProjectId, "save"))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save"
                    )
                }
            }
        }
    }

    private fun handleExport() {
        viewModelScope.launch {
            val exportProjectId = projectId ?: "export_${System.currentTimeMillis()}"
            _effect.emit(EditorEffect.NavigateToProcessing(exportProjectId, "export"))
        }
    }

    private fun handleShare() {
        viewModelScope.launch {
            val shareUri = "content://share/image.png"
            _effect.emit(EditorEffect.ShareImage(shareUri))
        }
    }
}