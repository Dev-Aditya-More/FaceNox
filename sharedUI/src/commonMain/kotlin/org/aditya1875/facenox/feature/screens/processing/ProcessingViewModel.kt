package org.aditya1875.facenox.feature.screens.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aditya1875.facenox.core.navigation.ProcessingOperation
import org.aditya1875.facenox.feature.screens.dashboard.Project
import org.aditya1875.facenox.feature.screens.dashboard.ProjectType
import org.aditya1875.facenox.feature.screens.editor.EditorSessionStore
import org.aditya1875.facenox.feature.screens.editor.EditorState
import org.aditya1875.facenox.platform.ImageProcessor
import org.aditya1875.facenox.platform.ProjectRepository

class ProcessingViewModel(
    private val projectId: String,
    private val operation: ProcessingOperation,
    private val imageProcessor: ImageProcessor,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val state: StateFlow<ProcessingState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProcessingEffect>()
    val effect: SharedFlow<ProcessingEffect> = _effect.asSharedFlow()

    init {
        startProcessing()
    }

    fun onEvent(event: ProcessingEvent) {
        when (event) {
            is ProcessingEvent.Start -> startProcessing()
            is ProcessingEvent.Cancel -> handleCancel()
            is ProcessingEvent.Retry -> handleRetry()
            is ProcessingEvent.Dismiss -> handleDismiss()
        }
    }

    private fun startProcessing() {
        viewModelScope.launch {
            val snapshot = EditorSessionStore.get(projectId) ?: run {
                _state.value = ProcessingState.Error("Missing editor session", canRetry = false)
                return@launch
            }

            val steps = getProcessingSteps()

            steps.dropLast(1).forEachIndexed { index, step ->
                _state.value = ProcessingState.Processing(
                    progress = (index + 1f) / steps.size,
                    currentStep = step,
                    totalSteps = steps.size
                )
                delay(300)
            }

            _state.value = ProcessingState.Processing(
                progress = (steps.size - 1f) / steps.size,
                currentStep = steps.last(),
                totalSteps = steps.size
            )

            val outputUri = try {
                val editorState = EditorState(
                    currentImage = snapshot.image,
                    brightness = snapshot.brightness,
                    contrast = snapshot.contrast,
                    saturation = snapshot.saturation,
                    appliedFilters = snapshot.appliedFilters,
                    drawingPaths = snapshot.drawingPaths,
                )
                imageProcessor.processAndSave(snapshot.image, editorState)
            } catch (e: Exception) {
                _state.value = ProcessingState.Error(e.message ?: "Processing failed", canRetry = true)
                return@launch
            }

            _state.value = ProcessingState.Success(outputUri = outputUri)

            val now = System.currentTimeMillis()
            val existing = projectRepository.get(projectId)
            projectRepository.upsert(
                Project(
                    id = projectId,
                    name = existing?.name ?: "Edit ${projectId.takeLast(4)}",
                    imageUri = outputUri,
                    createdAt = existing?.createdAt ?: now,
                    modifiedAt = now,
                    type = ProjectType.BASIC_EDIT
                )
            )

            _effect.emit(ProcessingEffect.NavigateToDashboard(showSuccess = true))
        }
    }

    private fun handleCancel() {
        viewModelScope.launch {
            _state.value = ProcessingState.Cancelled
            delay(1000)
            _effect.emit(ProcessingEffect.NavigateToDashboard(showSuccess = false))
        }
    }

    private fun handleRetry() { startProcessing() }

    private fun handleDismiss() {
        viewModelScope.launch { _effect.emit(ProcessingEffect.NavigateToDashboard(showSuccess = false)) }
    }

    private fun getProcessingSteps(): List<ProcessingStep> = when (operation) {
        ProcessingOperation.SAVE, ProcessingOperation.EXPORT -> listOf(
            ProcessingStep.LOADING,
            ProcessingStep.APPLYING_EDITS,
            ProcessingStep.APPLYING_FILTERS,
            ProcessingStep.COMPRESSING,
            ProcessingStep.SAVING
        )
        ProcessingOperation.SHARE -> listOf(
            ProcessingStep.LOADING,
            ProcessingStep.APPLYING_EDITS,
            ProcessingStep.COMPRESSING
        )
    }
}
