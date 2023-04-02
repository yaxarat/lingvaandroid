package dev.atajan.lingva_android.common.domain.results

import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo

sealed interface LanguagesRepositoryResponse {
    data class Success(val languageList: List<Language>) : LanguagesRepositoryResponse
    data class Failure(val errorMessage: String) : LanguagesRepositoryResponse
}

/**
 * TODO: Get TranslationWithInfo as a response and filter it to Translation model with extension function
 */
sealed interface TranslationRepositoryResponse {
    data class Success(val response: TranslationWithInfo) : TranslationRepositoryResponse
    data class Failure(val errorMessage: String) : TranslationRepositoryResponse
    object Loading : TranslationRepositoryResponse
}