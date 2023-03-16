package dev.atajan.lingva_android.usecases

import dev.atajan.lingva_android.common.data.api.entities.LanguagesEntity
import dev.atajan.lingva_android.common.data.api.entities.TranslationEntity
import dev.atajan.lingva_android.common.data.datasource.ApiResult

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
