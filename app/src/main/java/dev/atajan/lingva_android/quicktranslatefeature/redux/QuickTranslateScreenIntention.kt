package dev.atajan.lingva_android.quicktranslatefeature.redux

import dev.atajan.lingva_android.common.domain.models.language.Language

sealed interface QuickTranslateScreenIntention {
    data class OnTextToTranslateChange(val newValue: String) : QuickTranslateScreenIntention
    data class SetDefaultTargetLanguage(val languageName: String) : QuickTranslateScreenIntention
    data class SetNewSourceLanguage(val language: Language) : QuickTranslateScreenIntention
    data class SetNewTargetLanguage(val language: Language) : QuickTranslateScreenIntention
    data class ShowErrorDialog(val show: Boolean) : QuickTranslateScreenIntention
    data class SupportedLanguagesReceived(val languages: List<Language>) : QuickTranslateScreenIntention
    data class TranslationSuccess(val result: String) : QuickTranslateScreenIntention
    object CopyTextToClipboard : QuickTranslateScreenIntention
    object Translate : QuickTranslateScreenIntention
    object TranslationFailure : QuickTranslateScreenIntention
}