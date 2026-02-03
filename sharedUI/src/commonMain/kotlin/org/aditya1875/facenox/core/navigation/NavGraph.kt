package org.aditya1875.facenox.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.aditya1875.facenox.feature.screens.dashboard.DashboardScreen
import org.aditya1875.facenox.feature.screens.editor.EditorScreen
import org.aditya1875.facenox.feature.screens.imageselection.ImageSelectionScreen
import org.aditya1875.facenox.feature.screens.processing.ProcessingScreen
import org.aditya1875.facenox.feature.screens.splash.SplashScreen
import org.aditya1875.facenox.platform.ImagePicker
import org.aditya1875.facenox.platform.rememberImagePicker

@Composable
fun FaceNoxNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.Splash> {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(Route.Dashboard) {
                        popUpTo(Route.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Dashboard> {
            DashboardScreen(
                onNewProject = {
                    navController.navigate(Route.ImageSelection)
                },
                navController = navController,
                onOpenProject = { projectId, imageUri ->
                    navController.navigate(Route.Editor(projectId, imageUri))
                }
            )
        }

        composable<Route.ImageSelection> {

            val picker = rememberImagePicker()

            ImageSelectionScreen(
                onImageSelected = { imageUri ->
                    navController.navigate(Route.Editor(null, imageUri))
                },
                onBackClick = {
                    navController.navigateUp()
                },
                picker = picker
            )
        }

        composable<Route.Editor> { backStackEntry ->
            val editor: Route.Editor = backStackEntry.toRoute()
            EditorScreen(
                projectId = editor.projectId,
                imageUri = editor.imageUri,
                onBackClick = {
                    navController.navigateUp()
                },
                onSaveClick = { projectId ->
                    navController.navigate(
                        Route.Processing(projectId, ProcessingOperation.SAVE)
                    )
                },
                onExportClick = { projectId ->
                    navController.navigate(
                        Route.Processing(projectId, ProcessingOperation.EXPORT)
                    )
                }
            )
        }

        composable<Route.Processing> { backStackEntry ->
            val processing: Route.Processing = backStackEntry.toRoute()
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("PROJECT_SAVED", true)
            ProcessingScreen(
                projectId = processing.projectId,
                operation = processing.operation,
                onComplete = {
                    navController.navigate(Route.Dashboard) {
                        popUpTo(Route.Dashboard) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}