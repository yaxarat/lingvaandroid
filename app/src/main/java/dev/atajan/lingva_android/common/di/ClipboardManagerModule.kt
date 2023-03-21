package dev.atajan.lingva_android.common.di

import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClipboardManagerModule {

    @Singleton
    @Provides
    fun provideClipboardManager(@ApplicationContext application: Context): ClipboardManager {
        return application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}