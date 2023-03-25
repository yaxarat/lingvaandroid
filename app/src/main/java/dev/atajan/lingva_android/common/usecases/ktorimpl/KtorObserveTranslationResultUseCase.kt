package dev.atajan.lingva_android.common.usecases.ktorimpl

import dev.atajan.lingva_android.common.data.datasource.TranslationRepository
import dev.atajan.lingva_android.common.usecases.ObserveTranslationResultUseCase

class KtorObserveTranslationResultUseCase(private val translationRepository: TranslationRepository) : ObserveTranslationResultUseCase {

    override fun invoke() = translationRepository.translationResult
}