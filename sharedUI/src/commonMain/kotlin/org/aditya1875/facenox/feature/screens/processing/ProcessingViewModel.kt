package org.aditya1875.facenox.feature.screens.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aditya1875.facenox.core.navigation.ProcessingOperation

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
            _state.value = ProcessingState.Idle

            try {
                val steps = getProcessingSteps()

                steps.forEachIndexed { index, step ->
                    delay(800)
                    val progress = (index + 1) / steps.size.toFloat()
                    _state.value = ProcessingState.Processing(progress, step, steps.size)
                }

                delay(500)
                val outputUri = "content://facenox/output_$projectId.png"
                _state.value = ProcessingState.Success(outputUri)

                delay(2000)
                _effect.emit(ProcessingEffect.NavigateToDashboard(showSuccess = true))

            } catch (e: Exception) {
                _state.value = ProcessingState.Error(
                    message = e.message ?: "Processing failed",
                    canRetry = true
                )
            }
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