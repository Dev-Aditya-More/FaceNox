package org.aditya1875.facenox.feature.screens.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aditya1875.facenox.core.navigation.ProcessingOperation
import org.aditya1875.facenox.feature.screens.dashboard.Project
import org.aditya1875.facenox.feature.screens.dashboard.ProjectType
import org.aditya1875.facenox.feature.screens.dashboard.models.ProjectStore
import org.aditya1875.facenox.feature.screens.dashboard.models.ProjectSummary
import org.aditya1875.facenox.feature.screens.editor.EditorSessionStore
import org.aditya1875.facenox.platform.ImageProcessor

class ProcessingViewModel(
    private val projectId: String,
    private val operation: ProcessingOperation
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
            val snapshot = EditorSessionStore.get(projectId)
                ?: error("Missing editor session")

            val steps = getProcessingSteps()

            steps.forEachIndexed { index, step ->
                _state.value = ProcessingState.Processing(
                    progress = (index + 1) / steps.size.toFloat(),
                    currentStep = step,
                    totalSteps = steps.size
                )
                delay(300)
            }

            _state.value = ProcessingState.Success(
                outputUri = "content://facenox/output_$projectId.png"
            )

            val outputUri = "content://facenox/output_$projectId.png"

            val now = System.currentTimeMillis()

            val existing = ProjectStore.get(projectId)

            ProjectStore.upsert(
                Project(
                    id = projectId,
                    name = existing?.name ?: "Edit ${projectId.takeLast(4)}",
                    imageUri = outputUri,
                    thumbnail = null, // optional for now
                    createdAt = existing?.createdAt ?: now,
                    modifiedAt = now,
                    type = ProjectType.BASIC_EDIT
                )
            )

            _effect.emit(
                ProcessingEffect.NavigateToDashboard(showSuccess = true)
            )
        }
    }


    private fun handleCancel() {
        viewModelScope.launch {
            _state.value = ProcessingState.Cancelled
            delay(1000)
            _effect.emit(ProcessingEffect.NavigateToDashboard(showSuccess = false))
        }
    }

    private fun handleRetry() {
        startProcessing()
    }

    private fun handleDismiss() {
        viewModelScope.launch {
            _effect.emit(ProcessingEffect.NavigateToDashboard(showSuccess = false))
        }
    }

    private fun getProcessingSteps(): List<ProcessingStep> {
        return when (operation) {
            ProcessingOperation.SAVE -> listOf(
                ProcessingStep.LOADING,
                ProcessingStep.APPLYING_EDITS,
                ProcessingStep.APPLYING_FILTERS,
                ProcessingStep.COMPRESSING,
                ProcessingStep.SAVING
            )
            ProcessingOperation.EXPORT -> listOf(
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
}