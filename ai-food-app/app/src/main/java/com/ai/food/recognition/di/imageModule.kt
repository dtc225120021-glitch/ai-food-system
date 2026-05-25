package com.ai.food.recognition.di

import coil.ImageLoader
import coil.memory.MemoryCache
import org.koin.dsl.module

val imageModule = module {
    single {
        ImageLoader.Builder(get())
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(get())
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()
    }
}
