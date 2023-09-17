package dev.atajan.lingva_android.translatefeature.screens

import android.content.ClipData
import android.content.ClipboardManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.common.data.datasource.impl.APP_THEME
import dev.atajan.lingva_android.common.data.datasource.impl.CUSTOM_LINGVA_ENDPOINT
import dev.atajan.lingva_android.common.data.datasource.impl.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.common.data.datasource.impl.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.data.datasource.impl.LIVE_TRANSLATE_ENABLED
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.language.containsLanguageOrNull
import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.redux.MVIViewModel
import dev.atajan.lingva_android.common.redux.MiddleWare
import dev.atajan.lingva_android.common.redux.stateLogger
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.ObserveAudioDataUseCase
import dev.atajan.lingva_android.common.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.common.usecases.PlayByteArrayAudioUseCase
import dev.atajan.lingva_android.common.usecases.RequestAudioDataUseCase
import dev.atajan.lingva_android.common.usecases.TranslateUseCase
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ClearCustomLingvaServerUrl
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ClearInputField
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.CopyTextToClipboard
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DefaultSourceLanguageSelected
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DefaultTargetLanguageSelected
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.DisplayPronunciation
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ReadTextOutLoud
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetCustomLingvaServerUrl
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetDefaultSourceLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetDefaultTargetLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetLiveTranslate
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetNewSourceLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SetNewTargetLanguage
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ShowErrorDialog
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.SupportedLanguagesReceived
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ToggleAppTheme
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.Translate
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TranslationFailure
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TranslationSuccess
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.TrySwapLanguages
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.UserToggleLiveTranslate
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.UserUpdateCustomLingvaServerUrl
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenSideEffect
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    applicationScope: CoroutineScope,
    audioData: ObserveAudioDataUseCase,
    translationResult: ObserveTranslationResultUseCase,
    private val clipboardManager: ClipboardManager,
    private val dataStore: DataStore<Preferences>,
    private val supportedLanguages: FetchSupportedLanguagesUseCase,
    private val translate: TranslateUseCase,
    private val playByteArrayAudio: PlayByteArrayAudioUseCase,
    private val requestAudioData: RequestAudioDataUseCase,
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
    var textToTranslate = MutableStateFlow("")
        private set

    private var liveTranslateJob: Job? = null

    init {
        this.provideMiddleWares(stateLogger)

        viewModelScope.launch {
            getSupportedLanguages()
            observeDefaultLanguages(this)
        }
        observeIfCustomLingvaServerSet()
        observeTranslationResults(translationResult)
        observeAudioData(audioData)
        observeIfLiveTranslateEnabled()
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
                    textToTranslate = textToTranslate.value
                )
                currentState
            }
            TranslationFailure -> {
                send(ShowErrorDialog(true))
                currentState
            }
            is TranslationSuccess -> {
                updateSourceLanguageIfNewDetected(
                    currentSourceLanguageCode = currentState.sourceLanguage.code,
                    detectedSourceLanguageCode = intention.translationWithInfo.info.detectedSource
                )

                currentState.copy(
                    translatedText = intention.translationWithInfo.translation.result,
                    translatedTextPronunciation = intention.translationWithInfo.info.pronunciation,
                )
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
                viewModelScope.launch { textToTranslate.emit("") }
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
            ClearCustomLingvaServerUrl -> {
                updateCustomLingvaServer("")
                currentState
            }
            is UserUpdateCustomLingvaServerUrl -> {
                updateCustomLingvaServer(intention.url)
                currentState
            }
            is SetCustomLingvaServerUrl -> {
                currentState.copy(customLingvaServerUrl = intention.url)
            }
            ReadTextOutLoud -> {
                requestAudioData(
                    language = currentState.targetLanguage.code,
                    query = currentState.translatedText
                )
                currentState
            }
            DisplayPronunciation -> {
                if (!currentState.translatedTextPronunciation.isEmpty()) {
                    currentState.copy(displayPronunciation = !currentState.displayPronunciation)
                } else {
                    currentState
                }
            }

            is UserToggleLiveTranslate -> {
                updateLiveTranslatePreference(intention.enabled)
                currentState
            }

            is SetLiveTranslate -> {
                liveTranslate(intention.enabled)
                currentState.copy(liveTranslationEnabled = intention.enabled)
            }
        }
    }

    fun onTextToTranslateChange(newValue: String) {
        viewModelScope.launch { textToTranslate.emit(newValue) }
    }

    private fun updateLiveTranslatePreference(newValue: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[LIVE_TRANSLATE_ENABLED] = newValue
            }
        }
    }

    private fun updateCustomLingvaServer(url: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[CUSTOM_LINGVA_ENDPOINT] = url
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
            it[DEFAULT_SOURCE_LANGUAGE]
        }
            .distinctUntilChanged()
            .onEach {
                send(SetDefaultSourceLanguage(it))
            }
            .launchIn(scope)

        dataStore.data.mapNotNull {
            it[DEFAULT_TARGET_LANGUAGE]
        }
            .distinctUntilChanged()
            .onEach {
                send(SetDefaultTargetLanguage(it))
            }
            .launchIn(scope)
    }

    private fun observeTranslationResults(translationResult: ObserveTranslationResultUseCase) {
        translationResult().onEach {
            when (it) {
                is TranslationRepositoryResponse.Success -> {
                    send(TranslationSuccess(it.response))
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

    private fun getDefaultLanguageIfProvided(
        supportedLanguages: List<Language>,
        lookUpLanguage: String
    ): Language? {
        return supportedLanguages.find { it.name == lookUpLanguage }
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

    private fun observeIfCustomLingvaServerSet() {
        dataStore.data
            .mapNotNull { it[CUSTOM_LINGVA_ENDPOINT] }
            .distinctUntilChanged()
            .onEach {
                send(SetCustomLingvaServerUrl(it))
            }
            .launchIn(viewModelScope)
    }

    private fun observeIfLiveTranslateEnabled() {
        dataStore.data
            .map { it[LIVE_TRANSLATE_ENABLED] }
            .distinctUntilChanged()
            .onEach { enabled ->
                if (enabled == null || enabled == true) {
                    send(SetLiveTranslate(enabled = true))
                } else {
                    send(SetLiveTranslate(enabled = false))
                }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun liveTranslate(enable: Boolean) {
        liveTranslateJob = if (enable) {
            textToTranslate
                .drop(1)
                .debounce(300)
                .onEach {
                    if (it.isNotEmpty() && it.isNotBlank()) {
                        viewModelScope.launch { send(Translate)}
                    }
                }
                .launchIn(viewModelScope)
        } else {
            liveTranslateJob?.cancel("live translate has been toggled off")
            null
        }
    }
}
