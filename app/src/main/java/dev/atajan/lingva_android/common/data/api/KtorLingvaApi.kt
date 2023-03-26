package dev.atajan.lingva_android.common.data.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.atajan.lingva_android.common.data.api.lingvaDTOs.language.LanguagesDTO
import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO
import dev.atajan.lingva_android.common.data.datasource.CUSTOM_LINGVA_ENDPOINT
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError.BadCustomEndpoint
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError.BadEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class KtorLingvaApi @Inject constructor(
    private val androidHttpClient: HttpClient,
    private val dataStore: DataStore<Preferences>,
    private val endpoints: List<String>
) : LingvaApi {

    override suspend fun translate(
        source: String,
        target: String,
        query: String
    ): TranslationDTO {
        val customEndpoint = getCustomEndpoint()

        return if (customEndpoint.isNotEmpty()) {
            try {
                requestToEndpoint(
                    source = source,
                    target = target,
                    query = query,
                    endpoint = customEndpoint
                )
            } catch (e: Exception) {
                throw BadCustomEndpoint
            }
        } else {
            attemptTranslationRequest(
                source = source,
                target = target,
                query = query
            )
        }
    }

    override suspend fun getSupportedLanguages() = attemptSupportedLanguagesRequest()

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
        return androidHttpClient.get(endpoint) {
            url {
                appendPathSegments(source, target, escapeQuery(query))
            }
        }.body()
    }

    private suspend fun attemptSupportedLanguagesRequest(endpointIndex: Int = 0): LanguagesDTO {

        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpoints.lastIndex) throw BadEndpoints

        return try {
            androidHttpClient.get(endpoints[endpointIndex] + "languages/?:(source|target)").body()
        } catch (e: Exception) {
            attemptSupportedLanguagesRequest(endpointIndex = endpointIndex + 1)
        }
    }

    private fun escapeQuery(query: String): String {
        return query.replace("/", "%2F")
    }
}
