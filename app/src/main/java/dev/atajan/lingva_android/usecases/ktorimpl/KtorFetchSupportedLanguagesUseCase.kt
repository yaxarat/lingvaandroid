package dev.atajan.lingva_android.usecases.ktorimpl

import dev.atajan.lingva_android.common.data.datasource.LanguagesRepository
import dev.atajan.lingva_android.usecases.FetchSupportedLanguagesUseCase

class KtorFetchSupportedLanguagesUseCase(private val languagesRepository: LanguagesRepository) : FetchSupportedLanguagesUseCase {

    override suspend fun invoke() = languagesRepository.fetchSupportedLanguages()
}