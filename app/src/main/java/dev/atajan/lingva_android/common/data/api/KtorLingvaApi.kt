package dev.atajan.lingva_android.common.data.api

import dev.atajan.lingva_android.common.data.api.lingvaDTOs.language.LanguagesDTO
import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError.BadEndpoints
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError.TranslationFailure
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import javax.inject.Inject

class KtorLingvaApi @Inject constructor(
    private val androidHttpClient: HttpClient,
    private val endpoints: List<String>
) : LingvaApi {

    override suspend fun translate(
        source: String,
        target: String,
        query: String
    ) = attemptTranslationRequest(
        source = source,
        target = target,
        query = query
    )

    override suspend fun getSupportedLanguages() = attemptSupportedLanguagesRequest()

    private suspend fun attemptTranslationRequest(
        source: String,
        target: String,
        query: String,
        endpointIndex: Int = 0
    ): TranslationDTO {

        // Throw only when all fallback endpoints have been exhausted
        if (endpointIndex > endpoints.lastIndex) throw TranslationFailure

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
                endpointIndex = endpointIndex + 1
            )
        }
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
