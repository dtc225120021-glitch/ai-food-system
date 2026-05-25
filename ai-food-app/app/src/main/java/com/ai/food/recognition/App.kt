package com.ai.food.recognition

import android.app.Application
import com.ai.food.recognition.di.dataStoreModule
import com.ai.food.recognition.di.databaseModule
import com.ai.food.recognition.di.imageModule
import com.ai.food.recognition.di.networkModule
import com.ai.food.recognition.di.repositoryModule
import com.ai.food.recognition.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger()
            modules(
                databaseModule,
                dataStoreModule,
                repositoryModule,
                viewModelModule,
                imageModule,
                networkModule
            )
        }
    }
}