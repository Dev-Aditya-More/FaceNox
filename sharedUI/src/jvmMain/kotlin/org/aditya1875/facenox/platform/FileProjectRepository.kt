package org.aditya1875.facenox.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.aditya1875.facenox.feature.screens.dashboard.Project
import java.io.File

class FileProjectRepository(private val dataDir: String) : ProjectRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val file get() = File(dataDir, "projects.json")

    private fun readAll(): List<Project> {
        val f = file
        if (!f.exists()) return emptyList()
        return try {
            json.decodeFromString(f.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeAll(projects: List<Project>) {
        File(dataDir).mkdirs()
        file.writeText(json.encodeToString(projects))
    }

    override suspend fun upsert(project: Project) = withContext(Dispatchers.IO) {
        val current = readAll().toMutableList()
        current.removeAll { it.id == project.id }
        current.add(project)
        writeAll(current)
    }

    override suspend fun getAll(): List<Project> = withContext(Dispatchers.IO) {
        readAll().sortedByDescending { it.modifiedAt }
    }

    override suspend fun get(id: String): Project? = withContext(Dispatchers.IO) {
        readAll().find { it.id == id }
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        writeAll(readAll().filter { it.id != id })
    }
}
