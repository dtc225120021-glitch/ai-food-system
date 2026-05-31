package com.ai.food.recognition.di

import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.domain.model.local.LocalStorage
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val urlPath = originalRequest.url.encodedPath

            // Skip authorization header for auth endpoints
            if (urlPath.contains("/auth/")) {
                return@Interceptor chain.proceed(originalRequest)
            }

            var token = SessionManager.accessToken
            if (token.isNullOrEmpty()) {
                token = runBlocking {
                    get<LocalStorage>().token.firstOrNull()
                }
                if (!token.isNullOrEmpty()) {
                    SessionManager.accessToken = token
                }
            }

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(SessionManager.BASE_URL.plus("api/"))
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(AuthApi::class.java) }
}

