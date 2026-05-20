package org.aditya1875.facenox.feature.screens.dashboard

import kotlinx.serialization.Serializable


// ==================== MODELS ====================

@Serializable
data class Project(
    val id: String,
    val name: String,
    val imageUri: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val type: ProjectType
)

@Serializable
enum class ProjectType {
    BASIC_EDIT,
    FACE_CUT,
    BACKGROUND_REMOVED
}

/**
 * Dashboard statistics
 */
data class DashboardStats(
    val totalProjects: Int = 0,
    val imagesEdited: Int = 0,
    val facesCut: Int = 0
)

// ==================== STATE ====================

/**
 * Complete UI state for Dashboard
 */
data class DashboardState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: DashboardStats = DashboardStats()
)

// ==================== EVENT ====================

/**
 * All possible user actions on Dashboard
 */
sealed interface DashboardEvent {
    data object Refresh : DashboardEvent

    data object CreateNewProject : DashboardEvent

    data class OpenProject(val projectId: String) : DashboardEvent

    data class DeleteProject(val projectId: String) : DashboardEvent

    data class ConfirmDelete(val projectId: String) : DashboardEvent

    data object CancelDelete : DashboardEvent

    data object DismissError : DashboardEvent
}

// ==================== EFFECT ====================

sealed interface DashboardEffect {
    data object NavigateToImageSelection : DashboardEffect

    data class NavigateToEditor(
        val projectId: String,
        val imageUri: String
    ) : DashboardEffect

    data class ShowSnackbar(val message: String) : DashboardEffect

    data class ShowError(val message: String) : DashboardEffect
}