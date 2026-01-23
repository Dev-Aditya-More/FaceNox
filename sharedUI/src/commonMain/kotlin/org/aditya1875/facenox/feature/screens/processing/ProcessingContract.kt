package org.aditya1875.facenox.feature.screens.processing

import org.aditya1875.facenox.core.navigation.ProcessingOperation

sealed class ProcessingState {
    data object Idle : ProcessingState()

    data class Processing(
        val progress: Float,
        val currentStep: ProcessingStep,
        val totalSteps: Int
    ) : ProcessingState()

    data class Success(
        val outputUri: String,
        val message: String = "Processing complete!"
    ) : ProcessingState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : ProcessingState()

    data object Cancelled : ProcessingState()
}

// ==================== PROCESSING STEPS ====================

enum class ProcessingStep(val displayName: String) {
    LOADING("Loading image..."),
    APPLYING_EDITS("Applying edits..."),
    APPLYING_FILTERS("Applying filters..."),
    DETECTING_FACES("Detecting faces..."),
    CUTTING_FACES("Cutting faces..."),
    REMOVING_BACKGROUND("Removing background..."),
    COMPRESSING("Optimizing..."),
    SAVING("Saving...")
}

// ==================== METADATA ====================

data class ProcessingMetadata(
    val projectId: String,
    val operation: ProcessingOperation,
    val outputFormat: OutputFormat = OutputFormat.PNG,
    val quality: Int = 100
)

enum class OutputFormat {
    PNG,
    JPEG,
    WEBP
}

// ==================== EVENT ====================

sealed interface ProcessingEvent {
    data object Start : ProcessingEvent
    data object Cancel : ProcessingEvent
    data object Retry : ProcessingEvent
    data object Dismiss : ProcessingEvent
}

// ==================== EFFECT ====================

sealed interface ProcessingEffect {
    data class NavigateToDashboard(val showSuccess: Boolean = true) : ProcessingEffect
    data class ShareFile(val uri: String) : ProcessingEffect
    data class ShowSnackbar(val message: String) : ProcessingEffect
}