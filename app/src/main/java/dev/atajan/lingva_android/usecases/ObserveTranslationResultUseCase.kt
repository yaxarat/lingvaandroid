package dev.atajan.lingva_android.usecases

import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface ObserveTranslationResultUseCase {
    /**
     * Observes the translation result.
     */
    operator fun invoke(): Flow<TranslationRepositoryResponse>
}