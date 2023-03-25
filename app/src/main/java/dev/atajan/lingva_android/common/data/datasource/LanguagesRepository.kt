package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse

interface LanguagesRepository {

    suspend fun fetchSupportedLanguages(): LanguagesRepositoryResponse
}