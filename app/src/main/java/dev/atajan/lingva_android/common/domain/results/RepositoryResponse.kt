package dev.atajan.lingva_android.common.domain.results

import dev.atajan.lingva_android.common.domain.models.Translation
import dev.atajan.lingva_android.common.domain.models.language.Language

sealed interface LanguagesRepositoryResponse {
    data class Success(val languageList: List<Language>) : LanguagesRepositoryResponse
    data class Failure(val errorMessage: String) : LanguagesRepositoryResponse
    object Loading : LanguagesRepositoryResponse
}

sealed interface TranslationRepositoryResponse {
    data class Success(val translation: Translation) : TranslationRepositoryResponse
    data class Failure(val errorMessage: String) : TranslationRepositoryResponse
    object Loading : TranslationRepositoryResponse
}
