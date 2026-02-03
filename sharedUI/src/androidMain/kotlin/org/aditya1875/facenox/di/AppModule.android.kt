package org.aditya1875.facenox.di

import android.content.Context
import androidx.activity.ComponentActivity
import org.aditya1875.facenox.platform.AndroidImageLoader
import org.aditya1875.facenox.platform.AndroidImagePicker
import org.aditya1875.facenox.platform.AndroidImageProcessor
import org.aditya1875.facenox.platform.ImageLoader
import org.aditya1875.facenox.platform.ImagePicker
import org.aditya1875.facenox.platform.ImageProcessor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformAppModule = module {

    single<ImageLoader> { AndroidImageLoader(get()) }

    factory<ImagePicker> { (activity: ComponentActivity) ->
        AndroidImagePicker(activity)
    }
    single<ImageProcessor> {
        AndroidImageProcessor(
            context = androidContext()
        )
    }
}
