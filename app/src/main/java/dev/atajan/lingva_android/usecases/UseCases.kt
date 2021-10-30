package dev.atajan.lingva_android.usecases

import dev.atajan.lingva_android.api.entities.LanguagesEntity
import dev.atajan.lingva_android.api.entities.TranslationEntity
import dev.atajan.lingva_android.datasource.ApiResult

interface GetSupportedLanguagesUseCase {
    suspend operator fun invoke(): ApiResult<LanguagesEntity>
}

interface GetTranslationUseCase {
    suspend operator fun invoke(
        source: String,
        target: String,
        query: String
    ): ApiResult<TranslationEntity>
}
