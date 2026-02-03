package org.aditya1875.facenox.feature.screens.dashboard.models

import org.aditya1875.facenox.feature.screens.dashboard.Project

object ProjectStore {

    private val projects = mutableMapOf<String, Project>()

    fun upsert(project: Project) {
        projects[project.id] = project
    }

    fun getAll(): List<Project> =
        projects.values.sortedByDescending { it.modifiedAt }

    fun get(projectId: String): Project? =
        projects[projectId]

    fun delete(projectId: String) {
        projects.remove(projectId)
    }
}
