package dev.atajan.lingva_android.datasource

import com.github.michaelbull.result.runCatching
import dev.atajan.lingva_android.api.LingvaApi
import dev.atajan.lingva_android.api.entities.LanguagesEntity
import dev.atajan.lingva_android.api.entities.TranslationEntity

class LanguagesRepositoryImpl(private val api: LingvaApi) : LanguagesRepository {
    override suspend fun getSupportedLanguages(): ApiResult<LanguagesEntity> {
        return runCatching {
            api.getSupportedLanguages()
        }
    }
}

class TranslationRepositoryImpl(private val api: LingvaApi) : TranslationRepository {
    override suspend fun getTranslation(
        source: String,
        target: String,
        query: String
    ): ApiResult<TranslationEntity> {
        return runCatching {
            api.getTranslation(
                source = source,
                target = target,
                query = query
            )
        }
    }
}
