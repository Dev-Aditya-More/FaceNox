package org.aditya1875.facenox.feature.screens.editor

object EditorSessionStore {

    private val sessions = mutableMapOf<String, EditorSnapshot>()

    fun save(projectId: String, snapshot: EditorSnapshot) {
        sessions[projectId] = snapshot
    }

    fun get(projectId: String): EditorSnapshot? =
        sessions[projectId]

    fun clear(projectId: String) {
        sessions.remove(projectId)
    }
}
