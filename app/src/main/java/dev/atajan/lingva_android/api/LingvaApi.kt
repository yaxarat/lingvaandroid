package dev.atajan.lingva_android.api

import android.util.Log
import dev.atajan.lingva_android.api.entities.LanguagesEntity
import dev.atajan.lingva_android.api.entities.TranslationEntity
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object LingvaApi {
    private const val lingva = "https://lingva.ml/api/v1/"
    private const val plausibility = "https://translate.plausibility.cloud/api/v1/"
    private const val projectsegfau = "https://translate.projectsegfau.lt/api/v1/"
    private const val dr460nf1r3 = "https://translate.dr460nf1r3.org/api/v1/"
    private const val garudalinux = "https://lingva.garudalinux.org/api/v1/"

    private val endpointList = listOf(
        lingva,
        plausibility,
        projectsegfau,
        dr460nf1r3,
        garudalinux
    )

    private const val TIME_OUT = 5_000

    @OptIn(ExperimentalSerializationApi::class)
    private val ktorHttpClient = HttpClient(Android) {

        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
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
