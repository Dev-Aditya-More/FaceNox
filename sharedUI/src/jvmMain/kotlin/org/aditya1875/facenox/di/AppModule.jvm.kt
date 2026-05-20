package org.aditya1875.facenox.di

import org.aditya1875.facenox.platform.DesktopImageLoader
import org.aditya1875.facenox.platform.DesktopImagePicker
import org.aditya1875.facenox.platform.DesktopImageProcessor
import org.aditya1875.facenox.platform.DesktopFaceDetector
import org.aditya1875.facenox.platform.FaceDetector
import org.aditya1875.facenox.platform.FileProjectRepository
import org.aditya1875.facenox.platform.ImageLoader
import org.aditya1875.facenox.platform.ImagePicker
import org.aditya1875.facenox.platform.ImageProcessor
import org.aditya1875.facenox.platform.ProjectRepository
import org.koin.dsl.module

actual val platformAppModule = module {

    single<ImageLoader> { DesktopImageLoader() }

    single<ImageProcessor> { DesktopImageProcessor() }

    factory<ImagePicker> { DesktopImagePicker() }

    single<ProjectRepository> {
        val dir = "${System.getProperty("user.home")}/.facenox"
        FileProjectRepository(dir)
    }

    single<FaceDetector> { DesktopFaceDetector() }
}
