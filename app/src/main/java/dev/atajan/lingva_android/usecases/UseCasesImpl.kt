package dev.atajan.lingva_android.usecases

import dev.atajan.lingva_android.common.data.api.entities.LanguagesEntity
import dev.atajan.lingva_android.common.data.api.entities.TranslationEntity
import dev.atajan.lingva_android.common.data.datasource.ApiResult
import dev.atajan.lingva_android.common.data.datasource.LanguagesRepository
import dev.atajan.lingva_android.common.data.datasource.TranslationRepository

class GetSupportedLanguagesUseCaseImpl(private val languagesRepository: LanguagesRepository) : GetSupportedLanguagesUseCase {
    override suspend fun invoke(): ApiResult<LanguagesEntity> {
        return languagesRepository.getSupportedLanguages()
    }
}

class GetTranslationUseCaseImpl(private val translationRepository: TranslationRepository) : GetTranslationUseCase {
    override suspend fun invoke(
        source: String,
        target: String,
        query: String
    ): ApiResult<TranslationEntity> {
        return translationRepository.getTranslation(
            source = source,
            target = target,
            query = query
        )
    }
}