package dev.atajan.lingva_android.api

import android.util.Log
import dev.atajan.lingva_android.api.entities.LanguagesEntity
import dev.atajan.lingva_android.api.entities.TranslationEntity
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json

object LingvaApi {
    private const val lingva = "https://lingva.ml/api/v1/"
    private const val alefvanoon = "https://translate.alefvanoon.xyz/api/v1/"
    private const val igna = "https://translate.igna.rocks/api/v1/"
    private const val pussthecat = "https://lingva.pussthecat.org/api/v1/"

    private val endpointList = listOf(lingva, alefvanoon, igna, pussthecat)

    private const val TIME_OUT = 3_000

    private val ktorHttpClient = HttpClient(Android) {

        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )

            engine {
                connectTimeout = TIME_OUT
                socketTimeout = TIME_OUT
            }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Logger Ktor =>", message)
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP status:", "${response.status.value}")
            }
        }
    }

    suspend fun getTranslation(
        source: String,
        target: String,
        query: String
    ): TranslationEntity {
        return try {
            attemptTranslationRequest(
                source = source,
                target = target,
                query = query
            )
        } catch (e: Exception) {
            // TODO: add proper error logging
            TranslationEntity(
                translation = "Error Occurred",
                info = null
            )
        }
    }

    private suspend fun attemptTranslationRequest(
        source: String,
        target: String,
        query: String,
        endpointIndex: Int = 0,
        exception: Throwable? = null
    ): TranslationEntity {

        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpointList.lastIndex) {
            throw exception ?: Throwable("attemptTranslationRequest failed for all endpoints")
        }

        return try {
            ktorHttpClient.get("${endpointList[endpointIndex]}$source/$target/$query")
        } catch (e: Exception) {
            attemptTranslationRequest(
                source = source,
                target = target,
                query = query,
                endpointIndex = endpointIndex + 1,
                exception = e
            )
        }
    }

    suspend fun getSupportedLanguages(): LanguagesEntity {
        return  try {
            attemptSupportedLanguagesRequest()
        } catch (e: Exception) {
            // TODO: add proper error logging
            LanguagesEntity(emptyList())
        }
    }

    private suspend fun attemptSupportedLanguagesRequest(
        endpointIndex: Int = 0,
        exception: Throwable? = null
    ): LanguagesEntity {

        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpointList.lastIndex) {
            throw exception ?: Throwable("attemptSupportedLanguagesRequest failed for all endpoints")
        }

        return try {
            ktorHttpClient.get(endpointList[endpointIndex] + "languages/?:(source|target)")
        } catch (e: Exception) {
            attemptSupportedLanguagesRequest(
                endpointIndex = endpointIndex + 1,
                exception = e
            )
        }
    }
}
