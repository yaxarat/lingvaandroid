package dev.atajan.lingva_android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.datasource.LanguagesRepository
import dev.atajan.lingva_android.datasource.LanguagesRepositoryImpl
import dev.atajan.lingva_android.api.LingvaApi
import dev.atajan.lingva_android.datasource.TranslationRepository
import dev.atajan.lingva_android.datasource.TranslationRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideLanguagesRepository(api: LingvaApi): LanguagesRepository {
        return LanguagesRepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun provideTranslationRepository(api: LingvaApi): TranslationRepository {
        return TranslationRepositoryImpl(api)
    }
}