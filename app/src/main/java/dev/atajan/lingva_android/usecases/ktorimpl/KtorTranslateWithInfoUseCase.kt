package dev.atajan.lingva_android.usecases.ktorimpl

import dev.atajan.lingva_android.common.data.datasource.TranslationRepository
import dev.atajan.lingva_android.usecases.TranslateUseCase
import dev.atajan.lingva_android.usecases.TranslateWithInfoUseCase

class KtorTranslateWithInfoUseCase(private val translationRepository: TranslationRepository) : TranslateWithInfoUseCase {

    override fun invoke(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String
    ) {
        translationRepository.translate(
            source = sourceLanguageCode,
            target = targetLanguageCode,
            query = textToTranslate,
            requireInfo = true
        )
    }
}