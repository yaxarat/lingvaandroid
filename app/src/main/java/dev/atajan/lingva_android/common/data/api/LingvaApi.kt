package dev.atajan.lingva_android.common.data.api

import dev.atajan.lingva_android.common.data.api.lingvaDTOs.language.LanguagesDTO
import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO

interface LingvaApi {

    suspend fun translate(
        source: String,
        target: String,
        query: String
    ): TranslationDTO

    suspend fun getSupportedLanguages(): LanguagesDTO
}