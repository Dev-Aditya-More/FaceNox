package org.aditya1875.facenox.di

import org.aditya1875.facenox.platform.DesktopImageLoader
import org.aditya1875.facenox.platform.DesktopImagePicker
import org.aditya1875.facenox.platform.DesktopImageProcessor
import org.aditya1875.facenox.platform.ImageLoader
import org.aditya1875.facenox.platform.ImagePicker
import org.aditya1875.facenox.platform.ImageProcessor
import org.koin.dsl.module

actual val platformAppModule = module {

    single<ImageLoader> { DesktopImageLoader() }

    single<ImageProcessor> { DesktopImageProcessor() }

    factory<ImagePicker> {
        DesktopImagePicker()
    }
}