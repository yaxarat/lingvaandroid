package dev.atajan.lingva_android.quicktranslatefeature.screens

import android.content.ClipData
import android.content.ClipboardManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.common.data.datasource.impl.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo.Companion.toTranslation
import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.redux.MVIViewModel
import dev.atajan.lingva_android.common.redux.MiddleWare
import dev.atajan.lingva_android.common.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.ObserveAudioDataUseCase
import dev.atajan.lingva_android.common.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.common.usecases.PlayByteArrayAudioUseCase
import dev.atajan.lingva_android.common.usecases.RequestAudioDataUseCase
import dev.atajan.lingva_android.common.usecases.TranslateUseCase
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.CopyTextToClipboard
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.OnTextToTranslateChange
import dev.atajan.lingva_android.quicktranslatefeature.redux.QuickTranslateScreenIntention.ReadTextOutLoud
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickTranslateScreenViewModel @Inject constructor(
    applicationScope: CoroutineScope,
    translationResult: ObserveTranslationResultUseCase,
    audioData: ObserveAudioDataUseCase,
    private val clipboardManager: ClipboardManager,
    private val dataStore: DataStore<Preferences>,
    private val supportedLanguages: FetchSupportedLanguagesUseCase,
    private val translate: TranslateUseCase,
    private val playByteArrayAudio: PlayByteArrayAudioUseCase,
    private val requestAudioData: RequestAudioDataUseCase,
) : MVIViewModel<QuickTranslateScreenState, QuickTranslateScreenIntention, QuickTranslateScreenSideEffect>(
    scope = applicationScope,
    initialState = QuickTranslateScreenState()
) {

    init {
        observeTranslationResults(translationResult)
        viewModelScope.launch {
            // These operations need to be sequential
            getSupportedLanguages()
            observeDefaultLanguages(this)
        }
        observeAudioData(audioData)
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
                    textToTranslate = currentState.textToTranslate
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
            ReadTextOutLoud -> {
                requestAudioData(
                    language = currentState.targetLanguage.code,
                    query = currentState.translatedText
                )
                currentState
            }
        }
    }

    private suspend fun getSupportedLanguages() {
        supportedLanguages().let { result ->
            when (result) {
                is LanguagesRepositoryResponse.Success -> {
                    send(SupportedLanguagesReceived(result.languageList))
                }
                is LanguagesRepositoryResponse.Failure -> {
                    send(ShowErrorDialog(true))
                }
            }
        }
    }

    private fun observeDefaultLanguages(scope: CoroutineScope) {
        dataStore.data.mapNotNull {
            it[DEFAULT_TARGET_LANGUAGE]
        }
            .distinctUntilChanged()
            .onEach { send(SetDefaultTargetLanguage(it)) }
            .launchIn(scope)
    }

    private fun observeTranslationResults(translationResult: ObserveTranslationResultUseCase) {
        translationResult().onEach {
            when (it) {
                is TranslationRepositoryResponse.Success -> {
                    val responseWithInfo = it.response
                    val pureTranslationResult = responseWithInfo.toTranslation().result

                    send(TranslationSuccess(pureTranslationResult))
                }
                is TranslationRepositoryResponse.Failure -> {
                    send(TranslationFailure)
                }
                TranslationRepositoryResponse.Loading -> {
                    // Loading UI?
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun requestTranslation(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String,
    ) {
        viewModelScope.launch {
            translate(
                sourceLanguageCode = sourceLanguageCode,
                targetLanguageCode = targetLanguageCode,
                textToTranslate = textToTranslate
            )
        }
    }

    private fun copyTextToClipboard(translatedText: String) {
        val clipData = ClipData.newPlainText("Translation", translatedText)

        clipboardManager.setPrimaryClip(clipData)
    }

    private fun getDefaultLanguageIfProvided(
        supportedLanguages: List<Language>,
        lookUpLanguage: String
    ): Language? {
        return supportedLanguages.find { it.name == lookUpLanguage }
    }

    private fun observeAudioData(audioRepository: ObserveAudioDataUseCase) {
        audioRepository().onEach {
            when (it) {
                is AudioRepositoryResponse.Success -> {
                    playByteArrayAudio(it.audio.audioByteArray)
                }
                is AudioRepositoryResponse.Failure -> {
                    send(ShowErrorDialog(true))
                }
            }
        }.launchIn(viewModelScope)
    }
}