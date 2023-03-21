package dev.atajan.lingva_android.quicktranslatefeature.screens

import android.content.ClipData
import android.content.ClipboardManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.language.containsLanguageOrNull
import dev.atajan.lingva_android.common.redux.MVIViewModel
import dev.atajan.lingva_android.common.redux.MiddleWare
import dev.atajan.lingva_android.common.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.TranslateUseCase
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.CopyTextToClipboard
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.OnTextToTranslateChange
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.SetDefaultTargetLanguage
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.SetNewSourceLanguage
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.SetNewTargetLanguage
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.ShowErrorDialog
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.SupportedLanguagesReceived
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.Translate
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.TranslationFailure
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.TranslationSuccess
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenSideEffect
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenState
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class QuickTranslateScreenViewModel @Inject constructor(
    applicationScope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
    private val clipboardManager: ClipboardManager,
    private val getSupportedLanguages: FetchSupportedLanguagesUseCase,
    private val translate: TranslateUseCase,
) : MVIViewModel<QuickTranslateScreenState, QuickTranslateScreenIntention, QuickTranslateScreenSideEffect>(
    scope = applicationScope,
    initialState = QuickTranslateScreenState()
) {

    init {
//        viewModelScope.launch {
//            getSupportedLanguages().fold(
//                success = {
//                    send(SupportedLanguagesReceived(it.languages))
//                },
//                failure = {
//                    send(ShowErrorDialog(true))
//                }
//            )
//
//            dataStore.data.mapNotNull {
//                it[DEFAULT_TARGET_LANGUAGE]
//            }
//                .distinctUntilChanged()
//                .onEach {
//                    send(SetDefaultTargetLanguage(it))
//                }
//                .launchIn(this)
//
//            send(Translate)
//        }
    }

    override fun reduce(
        currentState: QuickTranslateScreenState,
        intention: QuickTranslateScreenIntention,
        middleWares: List<MiddleWare<QuickTranslateScreenState, QuickTranslateScreenIntention>>
    ): QuickTranslateScreenState {
        return when (intention) {
            is SetDefaultTargetLanguage -> {
                if (currentState.defaultTargetLanguage != intention.languageName) {
                    getDefaultLanguageIfProvided(
                        supportedLanguages = currentState.supportedLanguages,
                        lookUpLanguage = intention.languageName
                    ).let { language ->
                        currentState.copy(
                            targetLanguage = language ?: currentState.targetLanguage,
                            defaultTargetLanguage = language?.name ?: currentState.defaultTargetLanguage
                        )
                    }
                } else {
                    currentState
                }
            }
            is SetNewSourceLanguage -> currentState.copy(sourceLanguage = intention.language)
            is SetNewTargetLanguage -> currentState.copy(targetLanguage = intention.language)
            is ShowErrorDialog -> currentState.copy(errorDialogState = intention.show)
            is SupportedLanguagesReceived -> currentState.copy(supportedLanguages = intention.languages)
            is TranslationSuccess -> currentState.copy(translatedText = intention.result)
            Translate -> {
                requestTranslation(
                    sourceLanguageCode = currentState.sourceLanguage.code,
                    targetLanguageCode = currentState.targetLanguage.code,
                    textToTranslate = currentState.textToTranslate,
                    supportedLanguages = currentState.supportedLanguages
                )
                currentState
            }
            TranslationFailure -> currentState
            CopyTextToClipboard -> {
                copyTextToClipboard(currentState.translatedText)
                currentState
            }

            is OnTextToTranslateChange -> {
                currentState.copy(textToTranslate = intention.newValue)
            }
        }
    }

    private fun requestTranslation(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String,
        supportedLanguages: List<Language>
    ) {
//        viewModelScope.launch {
//            translate(
//                source = sourceLanguageCode,
//                target = targetLanguageCode,
//                query = textToTranslate
//            ).fold(
//                success = {
//                    send(TranslationSuccess(it.translation))
//
//                    if (sourceLanguageCode == "auto") {
//                        it.info?.detectedSource?.let { detectedSourceLanguageCode ->
//                            supportedLanguages
//                                .find { languageEntity ->
//                                    languageEntity.code == detectedSourceLanguageCode
//                                }
//                                ?.let { detectedSourceLanguage ->
//                                    send(
//                                        SetNewSourceLanguage(
//                                            detectedSourceLanguage
//                                        )
//                                    )
//                                }
//                        }
//                    }
//                },
//                failure = {
//                    send(TranslationFailure)
//                }
//            )
//        }
    }

    private fun copyTextToClipboard(translatedText: String) {
        val clipData = ClipData.newPlainText("Translation", translatedText)

        clipboardManager.setPrimaryClip(clipData)
    }

    private fun getDefaultLanguageIfProvided(
        supportedLanguages: List<Language>,
        lookUpLanguage: String
    ): Language? {
        return supportedLanguages.containsLanguageOrNull(lookUpLanguage)
    }
}