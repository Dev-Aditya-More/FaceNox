package org.aditya1875.facenox.di

import androidx.activity.ComponentActivity
import org.aditya1875.facenox.platform.AndroidImageLoader
import org.aditya1875.facenox.platform.AndroidImagePicker
import org.aditya1875.facenox.platform.AndroidImageProcessor
import org.aditya1875.facenox.platform.FaceDetector
import org.aditya1875.facenox.platform.FileProjectRepository
import org.aditya1875.facenox.platform.ImageLoader
import org.aditya1875.facenox.platform.ImagePicker
import org.aditya1875.facenox.platform.ImageProcessor
import org.aditya1875.facenox.platform.MlKitFaceDetector
import org.aditya1875.facenox.platform.ProjectRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformAppModule = module {

    single<ImageLoader> { AndroidImageLoader(get()) }

    factory<ImagePicker> { (activity: ComponentActivity) ->
        AndroidImagePicker(activity)
    }

    single<ImageProcessor> { AndroidImageProcessor(context = androidContext()) }

    single<ProjectRepository> {
        val dir = androidContext().filesDir.absolutePath + "/facenox"
        FileProjectRepository(dir)
    }

    single<FaceDetector> { MlKitFaceDetector() }
}
