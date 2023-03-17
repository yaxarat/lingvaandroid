package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {

    val translationResult: Flow<TranslationRepositoryResponse>

    fun translate(
        source: String,
        target: String,
        query: String
    )
}