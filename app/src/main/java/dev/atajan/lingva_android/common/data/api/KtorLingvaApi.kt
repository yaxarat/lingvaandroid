package dev.atajan.lingva_android.common.data.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.atajan.lingva_android.common.data.api.constants.AUDIO_PATH_SEGMENT
import dev.atajan.lingva_android.common.data.api.constants.SUPPORTED_LANGUAGE_PATH_SEGMENT
import dev.atajan.lingva_android.common.data.api.lingvadto.audio.AudioDTO
import dev.atajan.lingva_android.common.data.api.lingvadto.language.LanguagesDTO
import dev.atajan.lingva_android.common.data.api.lingvadto.translation.TranslationDTO
import dev.atajan.lingva_android.common.data.datasource.impl.CUSTOM_LINGVA_ENDPOINT
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError.BadCustomEndpoint
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError.BadEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class KtorLingvaApi @Inject constructor(
    private val androidHttpClient: HttpClient,
    private val dataStore: DataStore<Preferences>,
    private val endpoints: List<String>
) : LingvaApi {

    /**
     * Requesting list of supported languages from Lingva API
     */
    override suspend fun getSupportedLanguages() = attemptSupportedLanguagesRequest()

    private suspend fun attemptSupportedLanguagesRequest(endpointIndex: Int = 0): LanguagesDTO {

        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpoints.lastIndex) throw BadEndpoints

        return try {
            val response = androidHttpClient
                .get(endpoints[endpointIndex] + SUPPORTED_LANGUAGE_PATH_SEGMENT)

            if (response.isSuccessful()) return response.body() else throw BadEndpoints
        } catch (e: Exception) {
            attemptSupportedLanguagesRequest(endpointIndex = endpointIndex + 1)
        }
    }

    /**
     * Requesting translation from Lingva API
     */
    override suspend fun translate(
        source: String,
        target: String,
        query: String
    ): TranslationDTO {
        val customEndpoint = getCustomEndpoint()
        val encodedQuery = withContext(Dispatchers.IO) {
            escapeQuery(query.trim())
        }

        return if (customEndpoint.isNotEmpty()) {
            try {
                requestToEndpoint(
                    source = source,
                    target = target,
                    query = encodedQuery,
                    endpoint = customEndpoint
                )
            } catch (e: Exception) {
                throw BadCustomEndpoint
            }
        } else {
            attemptTranslationRequest(
                source = source,
                target = target,
                query = encodedQuery
            )
        }
    }

    private suspend fun getCustomEndpoint(): String {
        return dataStore.data.first()[CUSTOM_LINGVA_ENDPOINT] ?: ""
    }

    private suspend fun attemptTranslationRequest(
        source: String,
        target: String,
        query: String,
        endpointIndex: Int = 0
    ): TranslationDTO {
        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpoints.lastIndex) throw BadEndpoints

        return try {
            requestToEndpoint(
                source = source,
                target = target,
                query = query,
                endpoint = endpoints[endpointIndex]
            )
        } catch (e: Exception) {
            attemptTranslationRequest(
                source = source,
                target = target,
                query = query,
                endpointIndex = endpointIndex + 1
            )
        }
    }

    private suspend fun requestToEndpoint(
        source: String,
        target: String,
        query: String,
        endpoint: String
    ): TranslationDTO {
        val response = androidHttpClient.get(endpoint) {
            url {
                appendPathSegments(source, target, query)
            }
        }

        if (response.isSuccessful()) return response.body() else throw BadEndpoints
    }

    /**
     * Requesting audio from Lingva API
     */
    override suspend fun getAudio(
        language: String,
        query: String
    ): AudioDTO {
        return attemptAudioRequest(
            language = language,
            query = query
        )
    }

    private suspend fun attemptAudioRequest(
        language: String,
        query: String,
        endpointIndex: Int = 0
    ): AudioDTO {
        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpoints.lastIndex) throw BadEndpoints

        return try {
            requestToAudioEndoint(
                language = language,
                query = query,
                endpoint = endpoints[endpointIndex]
            )
        } catch (e: Exception) {
            attemptAudioRequest(
                language = language,
                query = query,
                endpointIndex = endpointIndex + 1
            )
        }
    }

    private suspend fun requestToAudioEndoint(
        language: String,
        query: String,
        endpoint: String
    ): AudioDTO {
        val response = androidHttpClient.get(endpoint + AUDIO_PATH_SEGMENT) {
            url {
                appendPathSegments(language, query)
            }
        }

        if (response.isSuccessful()) return response.body() else throw BadEndpoints
    }


    /**
     * Helper functions
     */
    private fun escapeQuery(query: String): String {
        return query.replace("/", "%2F")
    }

    private fun HttpResponse.isSuccessful() = status.value in 200..299
}
