package dev.atajan.lingva_android.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.common.data.datasource.LanguagesRepository
import dev.atajan.lingva_android.common.data.datasource.TranslationRepository
import dev.atajan.lingva_android.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.usecases.TranslateUseCase
import dev.atajan.lingva_android.usecases.TranslateWithInfoUseCase
import dev.atajan.lingva_android.usecases.ktorimpl.KtorFetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.ktorimpl.KtorObserveTranslationResultUseCase
import dev.atajan.lingva_android.usecases.ktorimpl.KtorTranslateUseCase
import dev.atajan.lingva_android.usecases.ktorimpl.KtorTranslateWithInfoUseCase
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
    fun provideKtorTranslateWithInfoUseCase(translationRepository: TranslationRepository): TranslateWithInfoUseCase {
        return KtorTranslateWithInfoUseCase(translationRepository)
    }

    @Singleton
    @Provides
    fun provideKtorObserveTranslationResultUseCase(translationRepository: TranslationRepository): ObserveTranslationResultUseCase {
        return KtorObserveTranslationResultUseCase(translationRepository)
    }
}