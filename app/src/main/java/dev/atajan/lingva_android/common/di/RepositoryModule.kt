package dev.atajan.lingva_android.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.data.datasource.AudioRepository
import dev.atajan.lingva_android.common.data.datasource.LanguagesRepository
import dev.atajan.lingva_android.common.data.datasource.TranslationRepository
import dev.atajan.lingva_android.common.data.datasource.impl.KtorAudioRepository
import dev.atajan.lingva_android.common.data.datasource.impl.KtorLanguagesRepository
import dev.atajan.lingva_android.common.data.datasource.impl.KtorTranslationRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideLanguagesRepository(api: KtorLingvaApi): LanguagesRepository {
        return KtorLanguagesRepository(api)
    }

    @Singleton
    @Provides
    fun provideTranslationRepository(
        api: KtorLingvaApi,
        applicationScope: CoroutineScope
    ): TranslationRepository {
        return KtorTranslationRepository(
            api = api,
            applicationScope = applicationScope
        )
    }

    @Singleton
    @Provides
    fun provideAudioRepository(
        api: KtorLingvaApi,
        applicationScope: CoroutineScope
    ): AudioRepository {
        return KtorAudioRepository(
            api = api,
            applicationScope = applicationScope
        )
    }
}