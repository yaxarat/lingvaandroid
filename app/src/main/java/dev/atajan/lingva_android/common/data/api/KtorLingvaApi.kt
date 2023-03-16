package dev.atajan.lingva_android.common.data.api

import dev.atajan.lingva_android.common.data.api.entities.LanguagesEntity
import dev.atajan.lingva_android.common.data.api.entities.TranslationEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import javax.inject.Inject

class KtorLingvaApi @Inject constructor(
    private val androidHttpClient: HttpClient,
    private val endpoints: List<String>
) : LingvaApi {

    override suspend fun getTranslation(
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
        if (endpointIndex > endpoints.lastIndex) {
            throw exception ?: Throwable("attemptTranslationRequest failed for all endpoints")
        }

        return try {
            androidHttpClient.get(endpoints[endpointIndex]) {
                url {
                    appendPathSegments(source, target, escapeQuery(query))
                }
            }.body()
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

    override suspend fun getSupportedLanguages(): LanguagesEntity {
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
        if (endpointIndex > endpoints.lastIndex) {
            throw exception ?: Throwable("attemptSupportedLanguagesRequest failed for all endpoints")
        }

        return try {
            androidHttpClient.get(endpoints[endpointIndex] + "languages/?:(source|target)").body()
        } catch (e: Exception) {
            attemptSupportedLanguagesRequest(
                endpointIndex = endpointIndex + 1,
                exception = e
            )
        }
    }

    private fun escapeQuery(query: String): String {
        return query.replace("/", "%2F")
    }
}
