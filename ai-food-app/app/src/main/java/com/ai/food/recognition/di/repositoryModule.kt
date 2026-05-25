package com.ai.food.recognition.di

import com.ai.food.recognition.data.repository.ScriptRepositoryImpl
import com.ai.food.recognition.domain.repository.ScriptRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ScriptRepository> {
        ScriptRepositoryImpl(
            get()
        )
    }
}