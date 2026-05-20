package org.aditya1875.facenox.feature.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.aditya1875.facenox.platform.ProjectRepository

class DashboardViewModel(private val projectRepository: ProjectRepository) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DashboardEffect>()
    val effect: SharedFlow<DashboardEffect> = _effect.asSharedFlow()

    init {
        loadProjects()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.Refresh -> loadProjects()
            is DashboardEvent.CreateNewProject -> handleCreateNewProject()
            is DashboardEvent.OpenProject -> handleOpenProject(event.projectId)
            is DashboardEvent.DeleteProject -> { /* handled by dialog in screen */ }
            is DashboardEvent.ConfirmDelete -> confirmDelete(event.projectId)
            is DashboardEvent.CancelDelete -> { /* no-op */ }
            is DashboardEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val projects = projectRepository.getAll()
                _state.update {
                    it.copy(
                        projects = projects,
                        stats = calculateStats(projects),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load projects") }
                _effect.emit(DashboardEffect.ShowError(e.message ?: "Failed to load projects"))
            }
        }
    }

    private fun handleCreateNewProject() {
        viewModelScope.launch { _effect.emit(DashboardEffect.NavigateToImageSelection) }
    }

    private fun handleOpenProject(projectId: String) {
        viewModelScope.launch {
            val project = _state.value.projects.find { it.id == projectId } ?: return@launch
            _effect.emit(DashboardEffect.NavigateToEditor(project.id, project.imageUri))
        }
    }

    private fun confirmDelete(projectId: String) {
        viewModelScope.launch {
            try {
                projectRepository.delete(projectId)
                _state.update {
                    val updated = it.projects.filter { p -> p.id != projectId }
                    it.copy(projects = updated, stats = calculateStats(updated))
                }
                _effect.emit(DashboardEffect.ShowSnackbar("Project deleted"))
            } catch (e: Exception) {
                _effect.emit(DashboardEffect.ShowError(e.message ?: "Failed to delete"))
            }
        }
    }

    private fun calculateStats(projects: List<Project>): DashboardStats {
        return DashboardStats(
            totalProjects = projects.size,
            imagesEdited = projects.size,
            facesCut = projects.count { it.type == ProjectType.FACE_CUT }
        )
    }
}
