package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface LanguagesRepository {

    val supportedLanguages: Flow<LanguagesRepositoryResponse>
    fun fetchSupportedLanguages()
}