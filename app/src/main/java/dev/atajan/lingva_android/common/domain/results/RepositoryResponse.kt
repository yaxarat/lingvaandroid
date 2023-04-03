package dev.atajan.lingva_android.common.domain.results

import dev.atajan.lingva_android.common.domain.models.audio.Audio
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo

sealed interface LanguagesRepositoryResponse {
    data class Success(val languageList: List<Language>) : LanguagesRepositoryResponse
    data class Failure(val errorMessage: String) : LanguagesRepositoryResponse
}

sealed interface TranslationRepositoryResponse {
    data class Success(val response: TranslationWithInfo) : TranslationRepositoryResponse
    data class Failure(val errorMessage: String) : TranslationRepositoryResponse
    object Loading : TranslationRepositoryResponse
}

sealed interface AudioRepositoryResponse {
    data class Success(val audio: Audio) : AudioRepositoryResponse
    data class Failure(val errorMessage: String) : AudioRepositoryResponse
}