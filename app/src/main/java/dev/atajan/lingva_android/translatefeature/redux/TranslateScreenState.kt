package dev.atajan.lingva_android.translatefeature.redux

import dev.atajan.lingva_android.common.domain.models.language.Language

data class TranslateScreenState(
    val defaultSourceLanguage: String = "",
    val defaultTargetLanguage: String = "",
    val displayPronunciation: Boolean = false,
    val errorDialogState: Boolean = false, // TODO: Should be a side effect.
    val liveTranslationEnabled: Boolean = true,
    val sourceLanguage: Language = Language("auto", "Detect"),
    val supportedLanguages: List<Language> = emptyList(),
    val targetLanguage: Language = Language("es", "Spanish"),
    val translatedText: String = "",
    val translatedTextPronunciation: String = "",
    val customLingvaServerUrl: String = "",
)
