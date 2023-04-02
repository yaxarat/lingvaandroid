package dev.atajan.lingva_android.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.common.data.datasource.LanguagesRepository
import dev.atajan.lingva_android.common.data.datasource.TranslationRepository
import dev.atajan.lingva_android.common.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.common.usecases.TranslateUseCase
import dev.atajan.lingva_android.common.usecases.ktorimpl.KtorFetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.ktorimpl.KtorObserveTranslationResultUseCase
import dev.atajan.lingva_android.common.usecases.ktorimpl.KtorTranslateUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Singleton
    @Provides
    fun provideKtorFetchSupportedLanguagesUseCase(languagesRepository: LanguagesRepository): FetchSupportedLanguagesUseCase {
        return KtorFetchSupportedLanguagesUseCase(languagesRepository)
    }

    @Singleton
    @Provides
    fun provideKtorTranslateUseCase(translationRepository: TranslationRepository): TranslateUseCase {
        return KtorTranslateUseCase(translationRepository)
    }

    @Singleton
    @Provides
    fun provideKtorObserveTranslationResultUseCase(translationRepository: TranslationRepository): ObserveTranslationResultUseCase {
        return KtorObserveTranslationResultUseCase(translationRepository)
    }
}