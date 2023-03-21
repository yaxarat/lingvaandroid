package dev.atajan.lingva_android.common.domain.results

import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.translation.Translation
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo

sealed interface LanguagesRepositoryResponse {
    data class Success(val languageList: List<Language>) : LanguagesRepositoryResponse
    data class Failure(val errorMessage: String) : LanguagesRepositoryResponse
}

sealed interface TranslationRepositoryResponse {
    data class TranslationSuccess(val response: Translation) : TranslationRepositoryResponse

    data class TranslationWithInfoSuccess(val response: TranslationWithInfo) : TranslationRepositoryResponse
    data class Failure(val errorMessage: String) : TranslationRepositoryResponse
    object Loading : TranslationRepositoryResponse
}