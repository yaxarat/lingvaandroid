package dev.atajan.lingva_android.translatefeature.mvi

import dev.atajan.lingva_android.common.domain.models.language.Language

data class TranslationScreenState(
    val supportedLanguages: List<Language> = emptyList(),
    val translatedText: String = "",
    val sourceLanguage: Language = Language("auto", "Detect"),
    val targetLanguage: Language = Language("es", "Spanish"),
    val textToTranslate: String = "",
    val errorDialogState: Boolean = false,
    val defaultSourceLanguage: String = "",
    val defaultTargetLanguage: String = "",
)
