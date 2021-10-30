package dev.atajan.lingva_android.datasource

import com.github.michaelbull.result.Result
import dev.atajan.lingva_android.api.entities.LanguagesEntity
import dev.atajan.lingva_android.api.entities.TranslationEntity

typealias ApiResult<T> = Result<T, Throwable>

interface LanguagesRepository {
    suspend fun getSupportedLanguages(): ApiResult<LanguagesEntity>
}

interface TranslationRepository {
    suspend fun getTranslation(
        source: String,
        target: String,
        query: String
    ): ApiResult<TranslationEntity>
}