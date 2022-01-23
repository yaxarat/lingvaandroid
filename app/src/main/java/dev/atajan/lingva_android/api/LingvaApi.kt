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
    private const val lingvaApiV1 = "https://lingva.ml/api/v1/"
    private const val fallback1 = "https://translate.alefvanoon.xyz/api/v1/"
    private const val fallback2 = "https://translate.igna.rocks/api/v1/"
    private const val fallback3 = "https://lingva.pussthecat.org/api/v1/"

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

    /**
     * Super dirty fallback logic below.. will be cleaned up later
     */

    suspend fun getTranslation(
        source: String,
        target: String,
        query: String
    ): TranslationEntity {
        return try {
            ktorHttpClient.get<TranslationEntity>("$lingvaApiV1$source/$target/$query")
        } catch (e: Exception) {
            getTranslationWithFallback1(source, target, query)
        }
    }

    private suspend fun getTranslationWithFallback1(
        source: String,
        target: String,
        query: String
    ): TranslationEntity {
        return try {
            ktorHttpClient.get<TranslationEntity>("$fallback1$source/$target/$query")
        } catch (e: Exception) {
            getTranslationWithFallback2(source, target, query)
        }
    }

    private suspend fun getTranslationWithFallback2(
        source: String,
        target: String,
        query: String
    ): TranslationEntity {
        return try {
            ktorHttpClient.get<TranslationEntity>("$fallback2$source/$target/$query")
        } catch (e: Exception) {
            getTranslationWithFallback3(source, target, query)
        }
    }

    private suspend fun getTranslationWithFallback3(
        source: String,
        target: String,
        query: String
    ): TranslationEntity {
        return ktorHttpClient.get<TranslationEntity>("$fallback3$source/$target/$query")
    }

    suspend fun getSupportedLanguages(): LanguagesEntity {
        return  try {
            ktorHttpClient.get(lingvaApiV1 + "languages/?:(source|target)")
        } catch (e: Exception) {
            getSupportedLanguagesWithFallback1()
        }
    }

    private suspend fun getSupportedLanguagesWithFallback1(): LanguagesEntity {
        return  try {
            ktorHttpClient.get(fallback1 + "languages/?:(source|target)")
        } catch (e: Exception) {
            getSupportedLanguagesWithFallback2()
        }
    }

    private suspend fun getSupportedLanguagesWithFallback2(): LanguagesEntity {
        return  try {
            ktorHttpClient.get(fallback2 + "languages/?:(source|target)")
        } catch (e: Exception) {
            getSupportedLanguagesWithFallback3()
        }
    }

    private suspend fun getSupportedLanguagesWithFallback3(): LanguagesEntity {
        return ktorHttpClient.get(fallback3 + "languages/?:(source|target)")
    }
}
