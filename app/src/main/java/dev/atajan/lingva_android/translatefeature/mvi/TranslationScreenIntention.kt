package dev.atajan.lingva_android.translatefeature.mvi

import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo
import dev.atajan.lingva_android.ui.theme.ThemingOptions

sealed interface TranslationScreenIntention {
    data class OnTextToTranslateChange(val newValue: String) : TranslationScreenIntention
    data class DefaultSourceLanguageSelected(val language: Language) : TranslationScreenIntention
    data class DefaultTargetLanguageSelected(val language: Language) : TranslationScreenIntention
    data class SetDefaultTargetLanguage(val languageName: String) : TranslationScreenIntention
    data class SetDefaultSourceLanguage(val languageName: String) : TranslationScreenIntention
    data class SetNewSourceLanguage(val language: Language) : TranslationScreenIntention
    data class SetNewTargetLanguage(val language: Language) : TranslationScreenIntention
    data class ShowErrorDialog(val show: Boolean) : TranslationScreenIntention
    data class SupportedLanguagesReceived(val languages: List<Language>) : TranslationScreenIntention
    data class TranslationSuccess(val translationWithInfo: TranslationWithInfo) : TranslationScreenIntention
    object ClearInputField : TranslationScreenIntention
    data class ToggleAppTheme(val newTheme: ThemingOptions) : TranslationScreenIntention
    object CopyTextToClipboard : TranslationScreenIntention
    object Translate : TranslationScreenIntention
    object TranslationFailure : TranslationScreenIntention
    object TrySwapLanguages : TranslationScreenIntention
}