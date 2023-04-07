package dev.atajan.lingva_android.translatefeature.redux

import dev.atajan.lingva_android.common.domain.models.language.Language

data class TranslateScreenState(
    val supportedLanguages: List<Language> = emptyList(),
    val translatedText: String = "",
    val translatedTextPronunciation: String = "",
    val sourceLanguage: Language = Language("auto", "Detect"),
    val targetLanguage: Language = Language("es", "Spanish"),
    val errorDialogState: Boolean = false,
    val displayPronunciation: Boolean = false,
    val defaultSourceLanguage: String = "",
    val defaultTargetLanguage: String = "",
)
