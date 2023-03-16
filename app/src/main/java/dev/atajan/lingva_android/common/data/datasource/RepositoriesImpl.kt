package dev.atajan.lingva_android.common.data.datasource

import com.github.michaelbull.result.runCatching
import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.data.api.entities.LanguagesEntity
import dev.atajan.lingva_android.common.data.api.entities.TranslationEntity

class LanguagesRepositoryImpl(private val api: KtorLingvaApi) : LanguagesRepository {
    override suspend fun getSupportedLanguages(): ApiResult<LanguagesEntity> {
        return runCatching {
            api.getSupportedLanguages()
        }
    }
}

class TranslationRepositoryImpl(private val api: KtorLingvaApi) : TranslationRepository {
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
