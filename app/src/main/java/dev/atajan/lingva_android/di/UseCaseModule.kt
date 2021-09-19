package dev.atajan.lingva_android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.datasource.LanguagesRepository
import dev.atajan.lingva_android.datasource.TranslationRepository
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCaseImpl
import dev.atajan.lingva_android.usecases.GetTranslationUseCase
import dev.atajan.lingva_android.usecases.GetTranslationUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Singleton
    @Provides
    fun provideGetSupportedLanguagesUseCase(languagesRepository: LanguagesRepository): GetSupportedLanguagesUseCase {
        return GetSupportedLanguagesUseCaseImpl(languagesRepository)
    }

    @Singleton
    @Provides
    fun provideGetTranslationUseCase(translationRepository: TranslationRepository): GetTranslationUseCase {
        return GetTranslationUseCaseImpl(translationRepository)
    }
}