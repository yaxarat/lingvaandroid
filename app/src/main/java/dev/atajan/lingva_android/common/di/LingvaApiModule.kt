package dev.atajan.lingva_android.common.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.common.constants.TRANSLATION_PROVIDERS
import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.data.api.LingvaApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LingvaApiModule {

    @OptIn(ExperimentalSerializationApi::class)
    @Singleton
    @Provides
    fun provideKotlinxSerializationConverter(): ContentConverter {
        return KotlinxSerializationConverter(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        )
    }

    @Singleton
    @Provides
    fun provideAndroidHttpClient(kotlinxSerializationConverter: ContentConverter): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                register(
                    contentType = ContentType.Application.Json,
                    converter = kotlinxSerializationConverter
                )
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Logger Ktor =>", message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    @Singleton
    @Provides
    fun provideTranslationEndpoints(): List<String> {
        return TRANSLATION_PROVIDERS
    }

    @Singleton
    @Provides
    fun provideLingvaApi(
        androidHttpClient: HttpClient,
        translationProviders: List<String>
    ): LingvaApi {
        return KtorLingvaApi(
            androidHttpClient = androidHttpClient,
            endpoints = translationProviders
        )
    }
}