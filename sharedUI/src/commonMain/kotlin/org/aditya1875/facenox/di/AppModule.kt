package org.aditya1875.facenox.di

import org.aditya1875.facenox.core.navigation.ProcessingOperation
import org.aditya1875.facenox.feature.screens.dashboard.DashboardViewModel
import org.aditya1875.facenox.feature.screens.editor.EditorViewModel
import org.aditya1875.facenox.feature.screens.imageselection.ImageSelectionViewModel
import org.aditya1875.facenox.feature.screens.processing.ProcessingViewModel
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect val platformAppModule: Module
@OptIn(KoinInternalApi::class)
val appModule = module {

    viewModel { DashboardViewModel() }

    viewModel { ImageSelectionViewModel() }

    viewModel { (projectId: String?, imageUri: String) ->
        EditorViewModel(
            projectId = projectId,
            imageUri = imageUri,
            imageLoader = get(),
            imageProcessor = get()
        )
    }

    viewModel { (projectId: String, operation: ProcessingOperation) ->
        ProcessingViewModel(
            projectId = projectId,
            operation = operation
        )
    }
}