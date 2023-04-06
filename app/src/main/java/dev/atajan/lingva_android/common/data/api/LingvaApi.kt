package dev.atajan.lingva_android.common.data.api

import dev.atajan.lingva_android.common.data.api.lingvadto.audio.AudioDTO
import dev.atajan.lingva_android.common.data.api.lingvadto.language.LanguagesDTO
import dev.atajan.lingva_android.common.data.api.lingvadto.translation.TranslationDTO

interface LingvaApi {

    suspend fun translate(
        source: String,
        target: String,
        query: String
    ): TranslationDTO

    suspend fun getSupportedLanguages(): LanguagesDTO

    suspend fun getAudio(
        language: String,
        query: String
    ): AudioDTO
}