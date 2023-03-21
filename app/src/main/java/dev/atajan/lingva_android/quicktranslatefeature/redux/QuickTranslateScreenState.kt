package dev.atajan.lingva_android.quicktranslatefeature.redux

import dev.atajan.lingva_android.common.domain.models.language.Language

data class QuickTranslateScreenState(
    val supportedLanguages: List<Language> = emptyList(),
    val translatedText: String = "",
    val sourceLanguage: Language = Language("auto", "Detect"),
    val targetLanguage: Language = Language("es", "Spanish"),
    val textToTranslate: String = "",
    val errorDialogState: Boolean = false,
    val defaultTargetLanguage: String = ""
)