package dev.atajan.lingva_android.common.data.api

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
        Log.d("yaxar", "$endpointIndex")
        Log.d("yaxar", "endpoints: ${endpoints.size}")
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

        if (response.status.value !in 200..299) throw BadEndpoints else return response.body()
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
