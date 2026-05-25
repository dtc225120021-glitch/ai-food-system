package com.ai.food.recognition.di

import androidx.room.Room
import com.ai.food.recognition.data.local.AppDatabase
import com.ai.food.recognition.data.local.datastore.DataStoreManager
import com.ai.food.recognition.domain.model.local.LocalStorage
import com.ai.food.recognition.ext.Constants
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            Constants.ROOM_DATABASE_NAME
        ).build()
    }
    single { get<AppDatabase>().scriptDao() }
}

val dataStoreModule = module {
    single<LocalStorage> {
        DataStoreManager(
            get()
        )
    }
}
