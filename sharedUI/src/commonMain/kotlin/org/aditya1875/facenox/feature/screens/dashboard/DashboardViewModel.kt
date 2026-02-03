package org.aditya1875.facenox.feature.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aditya1875.facenox.feature.screens.dashboard.models.ProjectStore

class DashboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DashboardEffect>()
    val effect: SharedFlow<DashboardEffect> = _effect.asSharedFlow()

    private var pendingDeleteProjectId: String? = null

    init {
        loadProjects()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.Refresh -> loadProjects()
            is DashboardEvent.CreateNewProject -> handleCreateNewProject()
            is DashboardEvent.OpenProject -> handleOpenProject(event.projectId)
            is DashboardEvent.DeleteProject -> handleDeleteProject(event.projectId)
            is DashboardEvent.ConfirmDelete -> confirmDelete()
            is DashboardEvent.CancelDelete -> cancelDelete()
            is DashboardEvent.DismissError -> dismissError()
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val projects = ProjectStore.getAll()
                val stats = calculateStats(projects)

                _state.update {
                    it.copy(
                        projects = projects,
                        stats = stats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load projects"
                    )
                }
                _effect.emit(
                    DashboardEffect.ShowError(e.message ?: "Failed to load projects")
                )
            }
        }
    }

    private fun handleCreateNewProject() {
        viewModelScope.launch {
            _effect.emit(DashboardEffect.NavigateToImageSelection)
        }
    }

    private fun handleOpenProject(projectId: String) {
        viewModelScope.launch {
            val project = _state.value.projects.find { it.id == projectId }
            if (project != null) {
                _effect.emit(DashboardEffect.NavigateToEditor(project.id, project.imageUri))
            }
        }
    }

    private fun handleDeleteProject(projectId: String) {
        pendingDeleteProjectId = projectId
    }

    private fun confirmDelete() {
        val projectId = pendingDeleteProjectId ?: return

        viewModelScope.launch {
            try {
                // TODO: Delete from repository

                _state.update {
                    val updatedProjects = it.projects.filter { project ->
                        project.id != projectId
                    }
                    it.copy(
                        projects = updatedProjects,
                        stats = calculateStats(updatedProjects)
                    )
                }

                _effect.emit(DashboardEffect.ShowSnackbar("Project deleted"))
                pendingDeleteProjectId = null
            } catch (e: Exception) {
                _effect.emit(DashboardEffect.ShowError(e.message ?: "Failed to delete"))
            }
        }
    }

    private fun cancelDelete() {
        pendingDeleteProjectId = null
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun calculateStats(projects: List<Project>): DashboardStats {
        return DashboardStats(
            totalProjects = projects.size,
            imagesEdited = projects.size,
            facesCut = projects.count { it.type == ProjectType.FACE_CUT }
        )
    }
}