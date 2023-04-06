package dev.atajan.lingva_android.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.atajan.lingva_android.common.media.AudioPlayer
import dev.atajan.lingva_android.common.media.NativeAudioPlayer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioPlayerModule {

    @Singleton
    @Provides
    fun provideAudioPlayer(): AudioPlayer {
        return NativeAudioPlayer()
    }
}