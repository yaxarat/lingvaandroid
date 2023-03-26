package dev.atajan.lingva_android.translatefeature.screens

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.common.data.datasource.APP_THEME
import dev.atajan.lingva_android.common.data.datasource.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.common.data.datasource.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.language.containsLanguageOrNull
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.redux.MVIViewModel
import dev.atajan.lingva_android.common.redux.MiddleWare
import dev.atajan.lingva_android.common.redux.stateLogger
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.common.usecases.TranslateWithInfoUseCase
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ClearInputField
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.CopyTextToClipboard
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DefaultSourceLanguageSelected
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DefaultTargetLanguageSelected
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetDefaultSourceLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetDefaultTargetLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetNewSourceLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetNewTargetLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ShowErrorDialog
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SupportedLanguagesReceived
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ToggleAppTheme
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.Translate
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TranslationFailure
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TranslationSuccess
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TrySwapLanguages
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenSideEffect
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    applicationScope: CoroutineScope,
    translationResult: ObserveTranslationResultUseCase,
    private val clipboardManager: ClipboardManager,
    private val dataStore: DataStore<Preferences>,
    private val supportedLanguages: FetchSupportedLanguagesUseCase,
    private val translate: TranslateWithInfoUseCase,
) : MVIViewModel<TranslateScreenState, TranslateScreenIntention, TranslateScreenSideEffect>(
    scope = applicationScope,
    initialState = TranslateScreenState()
) {

    private val stateLogger: MiddleWare<TranslateScreenState, TranslateScreenIntention> by lazy {
        object : MiddleWare<TranslateScreenState, TranslateScreenIntention> {
            override fun invoke(state: TranslateScreenState, intention: TranslateScreenIntention) {
                this@TranslateScreenViewModel.stateLogger(
                    state = state.toString(),
                    intention = intention.toString()
                )
            }
        }
    }

    /**
     * TODO: Find a better way to incorporate the text field state into the encompassing [TranslateScreenState]
     * Rough state management fix for text field:
     * https://medium.com/androiddevelopers/effective-state-management-for-textfield-in-compose-d6e5b070fbe5
     */
    var textToTranslate by mutableStateOf("")
        private set

    init {
        this.provideMiddleWares(stateLogger)
        getSupportedLanguages()
        observeDefaultLanguages()
        observeTranslationResults(translationResult)
    }

    override fun reduce(
        currentState: TranslateScreenState,
        intention: TranslateScreenIntention,
        middleWares: List<MiddleWare<TranslateScreenState, TranslateScreenIntention>>
    ): TranslateScreenState {

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
                    textToTranslate = textToTranslate
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
            is SetNewSourceLanguage -> currentState.copy(sourceLanguage = intention.language)
            is SetNewTargetLanguage -> currentState.copy(targetLanguage = intention.language)
            ClearInputField -> {
                textToTranslate = ""
                currentState
            }
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

    fun onTextToTranslateChange(newValue: String) {
        textToTranslate = newValue
    }

    private fun getSupportedLanguages() {
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
    }

    private fun observeDefaultLanguages() {
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
    }

    private fun observeTranslationResults(translationResult: ObserveTranslationResultUseCase) {
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
