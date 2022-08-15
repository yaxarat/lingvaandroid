package dev.atajan.lingva_android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.MainApplication
import dev.atajan.lingva_android.api.entities.LanguageEntity
import dev.atajan.lingva_android.datastore.APP_THEME
import dev.atajan.lingva_android.datastore.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.datastore.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.datastore.dataStore
import dev.atajan.lingva_android.mvi.MVIViewModel
import dev.atajan.lingva_android.mvi.MiddleWare
import dev.atajan.lingva_android.mvi.stateLogger
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.SideEffect
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.State
import dev.atajan.lingva_android.ui.theme.ThemingOptions
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.GetTranslationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    private val application: MainApplication,
    private val getSupportedLanguages: GetSupportedLanguagesUseCase,
    private val translate: GetTranslationUseCase,
    applicationScope: CoroutineScope
) : MVIViewModel<State, Intention, SideEffect>(
    scope = applicationScope,
    initialState = State()
) {

    private val dataStore = application.applicationContext.dataStore

    private val stateLogger: MiddleWare<State, Intention> by lazy {
        object : MiddleWare<State, Intention> {
            override fun invoke(state: State, intention: Intention) {
                this@TranslateScreenViewModel.stateLogger(
                    state = state.toString(), intention = intention.toString()
                )
            }
        }
    }

    init {
        this.provideMiddleWares(stateLogger)

        viewModelScope.launch {
            getSupportedLanguages().fold(
                success = {
                    send(Intention.SupportedLanguagesReceived(it.languages))
                },
                failure = {
                    send(Intention.ShowErrorDialog(true))
                }
            )

            dataStore.data.mapNotNull {
                it[DEFAULT_SOURCE_LANGUAGE]
            }
                .distinctUntilChanged()
                .onEach {
                    send(Intention.SetDefaultSourceLanguage(it))
                }
                .launchIn(this)

            dataStore.data.mapNotNull {
                it[DEFAULT_TARGET_LANGUAGE]
            }
                .distinctUntilChanged()
                .onEach {
                    send(Intention.SetDefaultTargetLanguage(it))
                }
                .launchIn(this)
        }
    }

    override fun reduce(
        currentState: State,
        intention: Intention,
        middleWares: List<MiddleWare<State, Intention>>
    ): State {

        middleWares.forEach {
            it.invoke(currentState, intention)
        }

        return when (intention) {
            is Intention.ShowErrorDialog -> currentState.copy(errorDialogState = intention.show)
            is Intention.SupportedLanguagesReceived -> {
                currentState.copy(supportedLanguages = intention.languages)
            }
            is Intention.DefaultSourceLanguageSelected -> {
                setDefaultSourceLanguage(intention.language)
                currentState
            }
            is Intention.DefaultTargetLanguageSelected -> {
                setDefaultTargetLanguage(intention.language)
                currentState
            }
            is Intention.Translate -> {
                requestTranslation(
                    sourceLanguageCode = currentState.sourceLanguage.code,
                    targetLanguageCode = currentState.targetLanguage.code,
                    textToTranslate = currentState.textToTranslate
                )
                currentState
            }
            Intention.TranslationFailure -> {
                // TODO: should probably show error
                currentState
            }
            is Intention.TranslationSuccess -> currentState.copy(translatedText = intention.result)
            Intention.CopyTextToClipboard -> {
                copyTextToClipboard(currentState.translatedText)
                currentState
            }
            Intention.TrySwapLanguages -> {
                return if (currentState.sourceLanguage != LanguageEntity("auto", "Detect")) {
                    currentState.copy(
                        sourceLanguage = currentState.targetLanguage,
                        targetLanguage = currentState.sourceLanguage
                    )
                } else {
                    currentState
                }
            }
            is Intention.OnTextToTranslateChange -> currentState.copy(textToTranslate = intention.newValue)
            is Intention.SetNewSourceLanguage -> currentState.copy(sourceLanguage = intention.language)
            is Intention.SetNewTargetLanguage -> currentState.copy(targetLanguage = intention.language)
            Intention.ClearInputField -> currentState.copy(textToTranslate = "")
            is Intention.ToggleAppTheme -> {
                toggleAppTheme(newTheme = intention.newTheme)
                currentState
            }
            is Intention.SetDefaultSourceLanguage -> {
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
            is Intention.SetDefaultTargetLanguage -> {
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
        supportedLanguages: List<LanguageEntity>,
        lookUpLanguage: String
    ): LanguageEntity? {
        return supportedLanguages.find { it.name == lookUpLanguage }
    }

    private fun requestTranslation(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String
    ) {
        viewModelScope.launch {
            translate(
                source = sourceLanguageCode,
                target = targetLanguageCode,
                query = textToTranslate
            ).fold(
                success = {
                    send(Intention.TranslationSuccess(it.translation))
                },
                failure = {
                    send(Intention.TranslationFailure)
                }
            )
        }
    }

    private fun copyTextToClipboard(translatedText: String) {
        val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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

    private fun setDefaultSourceLanguage(newLanguage: LanguageEntity) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DEFAULT_SOURCE_LANGUAGE] = newLanguage.name
            }
        }
    }

    private fun setDefaultTargetLanguage(newLanguage: LanguageEntity) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DEFAULT_TARGET_LANGUAGE] = newLanguage.name
            }
        }
    }

    data class State(
        val supportedLanguages: List<LanguageEntity> = emptyList(),
        val translatedText: String = "",
        val sourceLanguage: LanguageEntity = LanguageEntity("auto", "Detect"),
        val targetLanguage: LanguageEntity = LanguageEntity("es", "Spanish"),
        val textToTranslate: String = "",
        val errorDialogState: Boolean = false,
        val defaultSourceLanguage: String = "",
        val defaultTargetLanguage: String = "",
    )

    sealed interface Intention {
        class OnTextToTranslateChange(val newValue: String) : Intention
        class DefaultSourceLanguageSelected(val language: LanguageEntity) : Intention
        class DefaultTargetLanguageSelected(val language: LanguageEntity) : Intention
        class SetDefaultTargetLanguage(val languageName: String) : Intention
        class SetDefaultSourceLanguage(val languageName: String) : Intention
        class SetNewSourceLanguage(val language: LanguageEntity) : Intention
        class SetNewTargetLanguage(val language: LanguageEntity) : Intention
        class ShowErrorDialog(val show: Boolean) : Intention
        class SupportedLanguagesReceived(val languages: List<LanguageEntity>) : Intention
        class TranslationSuccess(val result: String) : Intention
        object ClearInputField : Intention
        object CopyTextToClipboard : Intention
        object Translate : Intention
        object TranslationFailure : Intention
        object TrySwapLanguages : Intention
        class ToggleAppTheme(val newTheme: ThemingOptions) : Intention
    }

    sealed class SideEffect
}
