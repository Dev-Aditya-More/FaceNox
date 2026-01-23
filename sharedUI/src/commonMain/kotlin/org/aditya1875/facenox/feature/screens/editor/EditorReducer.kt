package org.aditya1875.facenox.feature.screens.editor

object EditorReducer {

    private const val MAX_HISTORY_SIZE = 50

    fun reduce(state: EditorState, intent: EditorIntent): EditorState {
        return when (intent) {
            is EditorIntent.SelectTool -> state.copy(
                selectedTool = intent.tool,
                showToolPanel = true
            )

            is EditorIntent.ToggleToolPanel -> state.copy(
                showToolPanel = !state.showToolPanel
            )

            is EditorIntent.UpdateCropRect -> state.copy(
                cropRect = intent.rect
            )

            is EditorIntent.ApplyFilter -> {
                val newFilters = if (intent.filter in state.appliedFilters) {
                    state.appliedFilters
                } else {
                    state.appliedFilters + intent.filter
                }
                addToHistory(state.copy(appliedFilters = newFilters))
            }

            is EditorIntent.RemoveFilter -> {
                val newFilters = state.appliedFilters - intent.filter
                addToHistory(state.copy(appliedFilters = newFilters))
            }

            is EditorIntent.UpdateBrightness -> state.copy(
                brightness = intent.value.coerceIn(-1f, 1f)
            )

            is EditorIntent.UpdateContrast -> state.copy(
                contrast = intent.value.coerceIn(-1f, 1f)
            )

            is EditorIntent.UpdateSaturation -> state.copy(
                saturation = intent.value.coerceIn(-1f, 1f)
            )

            is EditorIntent.ChangeBrushSize -> state.copy(
                brushSize = intent.size.coerceIn(1f, 100f)
            )

            is EditorIntent.ChangeBrushColor -> state.copy(
                brushColor = intent.color
            )

            is EditorIntent.Undo -> {
                if (state.canUndo) {
                    val newIndex = (state.currentHistoryIndex - 1).coerceAtLeast(0)
                    restoreSnapshot(state, newIndex)
                } else {
                    state
                }
            }

            is EditorIntent.Redo -> {
                if (state.canRedo) {
                    val newIndex = (state.currentHistoryIndex + 1)
                        .coerceAtMost(state.historyStack.size - 1)
                    restoreSnapshot(state, newIndex)
                } else {
                    state
                }
            }

            is EditorIntent.ResetToOriginal -> {
                addToHistory(
                    state.copy(
                        cropRect = null,
                        brightness = 0f,
                        contrast = 0f,
                        saturation = 0f,
                        appliedFilters = emptyList(),
                        drawingPaths = emptyList()
                    )
                )
            }

            is EditorIntent.DismissError -> state.copy(error = null)

            else -> state
        }
    }

    private fun addToHistory(state: EditorState): EditorState {
        val snapshot = createSnapshot(state)

        val newHistory = if (state.currentHistoryIndex < state.historyStack.size - 1) {
            state.historyStack.subList(0, state.currentHistoryIndex + 1) + snapshot
        } else {
            state.historyStack + snapshot
        }

        val limitedHistory = if (newHistory.size > MAX_HISTORY_SIZE) {
            newHistory.drop(1)
        } else {
            newHistory
        }

        return state.copy(
            historyStack = limitedHistory,
            currentHistoryIndex = limitedHistory.size - 1,
            canUndo = limitedHistory.size > 1,
            canRedo = false
        )
    }

    private fun restoreSnapshot(state: EditorState, index: Int): EditorState {
        val snapshot = state.historyStack.getOrNull(index) ?: return state

        return state.copy(
            cropRect = snapshot.cropRect,
            brightness = snapshot.brightness,
            contrast = snapshot.contrast,
            saturation = snapshot.saturation,
            appliedFilters = snapshot.appliedFilters,
            drawingPaths = snapshot.drawingPaths,
            detectedFaces = snapshot.detectedFaces,
            currentHistoryIndex = index,
            canUndo = index > 0,
            canRedo = index < state.historyStack.size - 1
        )
    }

    private fun createSnapshot(state: EditorState): EditorSnapshot {
        return EditorSnapshot(
            timestamp = System.currentTimeMillis(),
            cropRect = state.cropRect,
            brightness = state.brightness,
            contrast = state.contrast,
            saturation = state.saturation,
            appliedFilters = state.appliedFilters,
            drawingPaths = state.drawingPaths,
            detectedFaces = state.detectedFaces
        )
    }
}