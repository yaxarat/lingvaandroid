package dev.atajan.lingva_android.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.common.data.datasource.LanguagesRepository
import dev.atajan.lingva_android.common.data.datasource.LanguagesRepositoryImpl
import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.data.datasource.TranslationRepository
import dev.atajan.lingva_android.common.data.datasource.TranslationRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideLanguagesRepository(api: KtorLingvaApi): LanguagesRepository {
        return LanguagesRepositoryImpl(api)
    }

    @Singleton
    @Provides
    fun provideTranslationRepository(api: KtorLingvaApi): TranslationRepository {
        return TranslationRepositoryImpl(api)
    }
}