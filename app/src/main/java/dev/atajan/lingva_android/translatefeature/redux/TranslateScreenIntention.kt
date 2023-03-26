package dev.atajan.lingva_android.translatefeature.redux

import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions

sealed interface TranslateScreenIntention {
    data class DefaultSourceLanguageSelected(val language: Language) : TranslateScreenIntention
    data class DefaultTargetLanguageSelected(val language: Language) : TranslateScreenIntention
    data class SetDefaultTargetLanguage(val languageName: String) : TranslateScreenIntention
    data class SetDefaultSourceLanguage(val languageName: String) : TranslateScreenIntention
    data class SetNewSourceLanguage(val language: Language) : TranslateScreenIntention
    data class SetNewTargetLanguage(val language: Language) : TranslateScreenIntention
    data class ShowErrorDialog(val show: Boolean) : TranslateScreenIntention
    data class SupportedLanguagesReceived(val languages: List<Language>) : TranslateScreenIntention
    data class TranslationSuccess(val translationWithInfo: TranslationWithInfo) : TranslateScreenIntention
    object ClearInputField : TranslateScreenIntention
    data class ToggleAppTheme(val newTheme: ThemingOptions) : TranslateScreenIntention
    object CopyTextToClipboard : TranslateScreenIntention
    object Translate : TranslateScreenIntention
    object TranslationFailure : TranslateScreenIntention
    object TrySwapLanguages : TranslateScreenIntention
    data class UpdateCustomLingvaServerUrl(val url: String) : TranslateScreenIntention
    object ClearCustomLingvaServerUrl : TranslateScreenIntention
}