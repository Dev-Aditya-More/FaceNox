package org.aditya1875.facenox.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Splash : Route

    @Serializable
    data object Dashboard : Route

    @Serializable
    data object ImageSelection : Route

    @Serializable
    data class Editor(
        val projectId: String? = null,
        val imageUri: String
    ) : Route

    @Serializable
    data class Processing(
        val projectId: String,
        val operationName: String   // stores ProcessingOperation.name — enums can't be NavType in KMP
    ) : Route
}

@Serializable
enum class ProcessingOperation {
    SAVE,
    EXPORT,
    SHARE
}
