package org.aditya1875.facenox.feature.screens.editor

import androidx.compose.ui.graphics.ImageBitmap

object EditorReducer {

    private const val MAX_HISTORY_SIZE = 50

    fun reduce(state: EditorState, intent: EditorIntent): EditorState {
        return when (intent) {

            is EditorIntent.SelectTool -> {
                val entering = intent.tool == EditorTool.CROP
                // When leaving ADJUST, bake the current slider values into history so Undo works correctly
                val base = if (state.selectedTool == EditorTool.ADJUST && intent.tool != EditorTool.ADJUST) {
                    addToHistory(state)
                } else state
                base.copy(
                    selectedTool = intent.tool,
                    isEditingMode = entering,
                    cropRect = if (entering) {
                        base.cropRect ?: base.currentImage?.let { img ->
                            androidx.compose.ui.geometry.Rect(0f, 0f, img.width.toFloat(), img.height.toFloat())
                        }
                    } else null
                )
            }

            is EditorIntent.ToggleToolPanel -> state.copy(showToolPanel = !state.showToolPanel)

            is EditorIntent.CloseToolOptions -> {
                val base = if (state.selectedTool == EditorTool.ADJUST) addToHistory(state) else state
                base.copy(selectedTool = EditorTool.SELECT, isEditingMode = false)
            }

            is EditorIntent.UpdateCropRect -> state.copy(cropRect = intent.rect)

            is EditorIntent.ApplyCrop,
            is EditorIntent.CancelCrop,
            is EditorIntent.RotateClockwise,
            is EditorIntent.RotateCounterClockwise,
            is EditorIntent.StartDrawing,
            is EditorIntent.ContinueDrawing,
            is EditorIntent.EndDrawing,
            is EditorIntent.DetectFaces,
            is EditorIntent.SelectFace,
            is EditorIntent.CutFaces,
            is EditorIntent.BlurAllFaces,
            is EditorIntent.CropToFace,
            is EditorIntent.Save,
            is EditorIntent.Export,
            is EditorIntent.Share -> state

            is EditorIntent.ApplyFilter -> {
                val newFilters = if (intent.filter in state.appliedFilters)
                    state.appliedFilters
                else
                    state.appliedFilters + intent.filter
                addToHistory(state.copy(appliedFilters = newFilters))
            }

            is EditorIntent.RemoveFilter ->
                addToHistory(state.copy(appliedFilters = state.appliedFilters - intent.filter))

            is EditorIntent.UpdateBrightness -> state.copy(brightness = intent.value.coerceIn(-1f, 1f))
            is EditorIntent.UpdateContrast   -> state.copy(contrast = intent.value.coerceIn(-1f, 1f))
            is EditorIntent.UpdateSaturation -> state.copy(saturation = intent.value.coerceIn(-1f, 1f))

            is EditorIntent.ChangeBrushSize  -> state.copy(brushSize = intent.size.coerceIn(1f, 100f))
            is EditorIntent.ChangeBrushColor -> state.copy(brushColor = intent.color)

            is EditorIntent.Undo -> {
                if (state.canUndo) {
                    val newIndex = (state.currentHistoryIndex - 1).coerceAtLeast(0)
                    restoreSnapshot(state, newIndex)
                } else state
            }

            is EditorIntent.Redo -> {
                if (state.canRedo) {
                    val newIndex = (state.currentHistoryIndex + 1)
                        .coerceAtMost(state.historyStack.size - 1)
                    restoreSnapshot(state, newIndex)
                } else state
            }

            is EditorIntent.ResetToOriginal -> {
                // originalImage is the pixel source of truth set at load time
                val orig = state.originalImage ?: return state
                addToHistory(
                    state.copy(
                        currentImage = orig,
                        cropRect = null,
                        brightness = 0f,
                        contrast = 0f,
                        saturation = 0f,
                        appliedFilters = emptyList(),
                        drawingPaths = emptyList(),
                        isEditingMode = false,
                        selectedTool = EditorTool.SELECT
                    )
                )
            }

            is EditorIntent.DismissError -> state.copy(error = null)

            else -> state
        }
    }

    // -----------------------------------------------------------------------
    // History helpers
    // -----------------------------------------------------------------------

    fun addToHistory(state: EditorState): EditorState {
        val snapshot = createSnapshot(state)

        // If we're mid-history (after an undo), branch from here — drop the future
        val base = if (state.currentHistoryIndex < state.historyStack.size - 1) {
            state.historyStack.subList(0, state.currentHistoryIndex + 1)
        } else {
            state.historyStack
        }

        val newHistory = (base + snapshot).let { list ->
            if (list.size > MAX_HISTORY_SIZE) list.drop(1) else list
        }

        return state.copy(
            historyStack = newHistory,
            currentHistoryIndex = newHistory.size - 1,
            canUndo = newHistory.size > 1,
            canRedo = false
        )
    }

    private fun restoreSnapshot(state: EditorState, index: Int): EditorState {
        val snapshot = state.historyStack.getOrNull(index) ?: return state
        return state.copy(
            currentImage = snapshot.image,           // ← restores actual pixels
            cropRect = snapshot.cropRect,
            brightness = snapshot.brightness,
            contrast = snapshot.contrast,
            saturation = snapshot.saturation,
            appliedFilters = snapshot.appliedFilters,
            drawingPaths = snapshot.drawingPaths,
            detectedFaces = snapshot.detectedFaces,
            hasBackgroundRemoved = snapshot.hasBackgroundRemoved,
            currentHistoryIndex = index,
            canUndo = index > 0,
            canRedo = index < state.historyStack.size - 1,
            isEditingMode = false,
            selectedTool = EditorTool.SELECT
        )
    }

    fun createSnapshot(state: EditorState): EditorSnapshot {
        return EditorSnapshot(
            timestamp = System.currentTimeMillis(),
            // Snapshot the current pixels — required so undo restores the bitmap
            image = state.currentImage ?: state.originalImage ?: ImageBitmap(1, 1),
            cropRect = state.cropRect,
            brightness = state.brightness,
            contrast = state.contrast,
            saturation = state.saturation,
            appliedFilters = state.appliedFilters,
            drawingPaths = state.drawingPaths,
            detectedFaces = state.detectedFaces,
            hasBackgroundRemoved = state.hasBackgroundRemoved
        )
    }
}