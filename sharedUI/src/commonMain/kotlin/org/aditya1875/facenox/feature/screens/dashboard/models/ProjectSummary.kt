package org.aditya1875.facenox.feature.screens.dashboard.models

data class ProjectSummary(
    val projectId: String,
    val previewUri: String?,
    val editCount: Int,
    val lastUpdated: Long
)
