package dev.atajan.lingva_android.translatefeature.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.atajan.lingva_android.common.data.datasource.APP_THEME
import dev.atajan.lingva_android.common.data.datasource.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.common.data.datasource.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.data.datasource.dataStore
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.language.containsLanguageOrNull
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.mvi.MVIViewModel
import dev.atajan.lingva_android.common.mvi.MiddleWare
import dev.atajan.lingva_android.common.mvi.stateLogger
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.ClearInputField
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.CopyTextToClipboard
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.DefaultSourceLanguageSelected
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.DefaultTargetLanguageSelected
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.OnTextToTranslateChange
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.SetDefaultSourceLanguage
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.SetDefaultTargetLanguage
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.SetNewSourceLanguage
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.SetNewTargetLanguage
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.ShowErrorDialog
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.SupportedLanguagesReceived
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.ToggleAppTheme
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.Translate
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.TranslationFailure
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.TranslationSuccess
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenIntention.TrySwapLanguages
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenSideEffect
import dev.atajan.lingva_android.translatefeature.mvi.TranslationScreenState
import dev.atajan.lingva_android.ui.theme.ThemingOptions
import dev.atajan.lingva_android.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.usecases.TranslateWithInfoUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    @ApplicationContext application: Context,
    private val supportedLanguages: FetchSupportedLanguagesUseCase,
    private val translate: TranslateWithInfoUseCase,
    translationResult: ObserveTranslationResultUseCase,
    applicationScope: CoroutineScope,
) : MVIViewModel<TranslationScreenState, TranslationScreenIntention, TranslationScreenSideEffect>(
    scope = applicationScope,
    initialState = TranslationScreenState()
) {
    private val dataStore = application.applicationContext.dataStore

    private val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val stateLogger: MiddleWare<TranslationScreenState, TranslationScreenIntention> by lazy {
        object : MiddleWare<TranslationScreenState, TranslationScreenIntention> {
            override fun invoke(state: TranslationScreenState, intention: TranslationScreenIntention) {
                this@TranslateScreenViewModel.stateLogger(
                    state = state.toString(),
                    intention = intention.toString()
                )
            }
        }
    }

    init {
        this.provideMiddleWares(stateLogger)

        viewModelScope.launch {
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

        dataStore.data.mapNotNull {
            it[DEFAULT_SOURCE_LANGUAGE]
        }
            .distinctUntilChanged()
            .onEach {
                send(SetDefaultSourceLanguage(it))
            }
            .launchIn(viewModelScope)

        dataStore.data.mapNotNull {
            it[DEFAULT_TARGET_LANGUAGE]
        }
            .distinctUntilChanged()
            .onEach {
                send(SetDefaultTargetLanguage(it))
            }
            .launchIn(viewModelScope)

        translationResult().onEach {
            when (it) {
                is TranslationRepositoryResponse.TranslationWithInfoSuccess -> {
                    send(TranslationSuccess(it.response))
                }
                is TranslationRepositoryResponse.Failure -> {
                    send(TranslationFailure)
                }
                TranslationRepositoryResponse.Loading -> {
                    // Loading UI?
                }
                else -> { /* Do nothing */ }
            }
        }.launchIn(viewModelScope)
    }

    override fun reduce(
        currentState: TranslationScreenState,
        intention: TranslationScreenIntention,
        middleWares: List<MiddleWare<TranslationScreenState, TranslationScreenIntention>>
    ): TranslationScreenState {

        middleWares.forEach {
            it.invoke(currentState, intention)
        }

        return when (intention) {
            is ShowErrorDialog -> currentState.copy(errorDialogState = intention.show)
            is SupportedLanguagesReceived -> currentState.copy(supportedLanguages = intention.languages)
            is DefaultSourceLanguageSelected -> {
                setDefaultSourceLanguage(intention.language)
                currentState
            }
            is DefaultTargetLanguageSelected -> {
                setDefaultTargetLanguage(intention.language)
                currentState
            }
            is Translate -> {
                requestTranslation(
                    sourceLanguageCode = currentState.sourceLanguage.code,
                    targetLanguageCode = currentState.targetLanguage.code,
                    textToTranslate = currentState.textToTranslate
                )
                currentState
            }
            TranslationFailure -> {
                // TODO: should probably show & log the error
                currentState
            }
            is TranslationSuccess -> {
                updateSourceLanguageIfNewDetected(
                    currentSourceLanguageCode = currentState.sourceLanguage.code,
                    detectedSourceLanguageCode = intention.translationWithInfo.info.detectedSource
                )
                currentState.copy(translatedText = intention.translationWithInfo.translation.result)
            }
            CopyTextToClipboard -> {
                copyTextToClipboard(currentState.translatedText)
                currentState
            }
            TrySwapLanguages -> {
                return if (currentState.sourceLanguage != Language("auto", "Detect")) {
                    currentState.copy(
                        sourceLanguage = currentState.targetLanguage,
                        targetLanguage = currentState.sourceLanguage
                    )
                } else {
                    currentState
                }
            }
            is OnTextToTranslateChange -> currentState.copy(textToTranslate = intention.newValue)
            is SetNewSourceLanguage -> currentState.copy(sourceLanguage = intention.language)
            is SetNewTargetLanguage -> currentState.copy(targetLanguage = intention.language)
            ClearInputField -> currentState.copy(textToTranslate = "")
            is ToggleAppTheme -> {
                toggleAppTheme(newTheme = intention.newTheme)
                currentState
            }
            is SetDefaultSourceLanguage -> {
                if (currentState.defaultSourceLanguage != intention.languageName) {
                    getDefaultLanguageIfProvided(
                        supportedLanguages = currentState.supportedLanguages,
                        lookUpLanguage = intention.languageName
                    ).let { language ->
                        currentState.copy(
                            sourceLanguage = language ?: currentState.sourceLanguage,
                            defaultSourceLanguage = language?.name ?: currentState.defaultSourceLanguage
                        )
                    }
                } else {
                    currentState
                }
            }
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
        }
    }

    private fun getDefaultLanguageIfProvided(
        supportedLanguages: List<Language>,
        lookUpLanguage: String
    ): Language? {
        return supportedLanguages.containsLanguageOrNull(lookUpLanguage)
    }

    private fun requestTranslation(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String,
    ) {
        translate(
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
            textToTranslate = textToTranslate
        )
    }

    private fun copyTextToClipboard(translatedText: String) {
        val clipData = ClipData.newPlainText("Translation", translatedText)

        clipboardManager.setPrimaryClip(clipData)
    }

    private fun toggleAppTheme(newTheme: ThemingOptions) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[APP_THEME] = newTheme.name
            }
        }
    }

    private fun setDefaultSourceLanguage(newLanguage: Language) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DEFAULT_SOURCE_LANGUAGE] = newLanguage.name
            }
        }
    }

    private fun setDefaultTargetLanguage(newLanguage: Language) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DEFAULT_TARGET_LANGUAGE] = newLanguage.name
            }
        }
    }

    private fun updateSourceLanguageIfNewDetected(
        currentSourceLanguageCode: String,
        detectedSourceLanguageCode: String
    ) {
        viewModelScope.launch {
            if (currentSourceLanguageCode == "auto") {
                when (val result = supportedLanguages()) {
                    is LanguagesRepositoryResponse.Success -> {
                        result.languageList
                            .containsLanguageOrNull(detectedSourceLanguageCode)
                            ?.let { send(SetNewSourceLanguage(it))}
                    }
                    is LanguagesRepositoryResponse.Failure -> {
                        send(ShowErrorDialog(true))
                    }
                }
            }
        }
    }
}
