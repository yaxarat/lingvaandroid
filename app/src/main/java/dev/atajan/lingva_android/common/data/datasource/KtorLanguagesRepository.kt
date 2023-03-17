package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError
import dev.atajan.lingva_android.common.domain.models.language.Language.Companion.toDomainModel
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse.Failure
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse.Success

class KtorLanguagesRepository(private val api: KtorLingvaApi) : LanguagesRepository {

    override suspend fun fetchSupportedLanguages(): LanguagesRepositoryResponse {
        val supportedLanguagesDTO = try {
            api.getSupportedLanguages()
        } catch (error: LingvaApiError) {
            return Failure(error.message)
        }

        with(supportedLanguagesDTO) {
            if (languages.isNullOrEmpty()) {
                return Failure("Languages can't be null or empty")
            }

            languages.let { languages ->
                return try {
                    languages
                        .map { it.toDomainModel() }
                        .let { Success(it) }
                } catch (error: DTOToDomainModelMappingError) {
                    Failure(error.message)
                }
            }
        }
    }
}
