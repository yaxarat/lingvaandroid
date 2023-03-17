package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {

    /**
     * [Flow] of the translation result from [translate].
     */
    val translationResult: Flow<TranslationRepositoryResponse>

    /**
     * Translates the [query] from [source] to [target].
     */
    fun translate(
        source: String,
        target: String,
        query: String
    )
}