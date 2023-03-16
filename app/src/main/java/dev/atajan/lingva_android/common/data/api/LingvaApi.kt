package dev.atajan.lingva_android.common.data.api

import dev.atajan.lingva_android.common.data.api.entities.LanguagesEntity
import dev.atajan.lingva_android.common.data.api.entities.TranslationEntity

interface LingvaApi {

    suspend fun getTranslation(
        source: String,
        target: String,
        query: String
    ): TranslationEntity

    suspend fun getSupportedLanguages(): LanguagesEntity
}