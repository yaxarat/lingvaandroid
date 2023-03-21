package dev.atajan.lingva_android.common.usecases

import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse

interface FetchSupportedLanguagesUseCase {
    /**
     * Requests for a list of supported languages.
     */
    suspend operator fun invoke(): LanguagesRepositoryResponse
}