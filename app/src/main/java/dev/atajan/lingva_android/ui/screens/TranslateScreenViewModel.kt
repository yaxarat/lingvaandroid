package dev.atajan.lingva_android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.atajan.lingva_android.MainApplication
import dev.atajan.lingva_android.common.data.api.entities.LanguageEntity
import dev.atajan.lingva_android.common.data.datastore.APP_THEME
import dev.atajan.lingva_android.common.data.datastore.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.common.data.datastore.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.data.datastore.dataStore
import dev.atajan.lingva_android.common.mvi.MVIViewModel
import dev.atajan.lingva_android.common.mvi.MiddleWare
import dev.atajan.lingva_android.common.mvi.stateLogger
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.ClearInputField
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.CopyTextToClipboard
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.DefaultSourceLanguageSelected
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.DefaultTargetLanguageSelected
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.OnTextToTranslateChange
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SetDefaultSourceLanguage
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SetDefaultTargetLanguage
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SetNewSourceLanguage
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SetNewTargetLanguage
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.ShowErrorDialog
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.SupportedLanguagesReceived
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.ToggleAppTheme
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.Translate
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.TranslationFailure
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.TranslationSuccess
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention.TrySwapLanguages
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
    @ApplicationContext application: Context,
    private val getSupportedLanguages: GetSupportedLanguagesUseCase,
    private val translate: GetTranslationUseCase,
    applicationScope: CoroutineScope
) : MVIViewModel<State, Intention, SideEffect>(
    scope = applicationScope,
    initialState = State()
) {
    private val dataStore = application.applicationContext.dataStore
    private val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

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
                    send(SupportedLanguagesReceived(it.languages))
                },
                failure = {
                    send(ShowErrorDialog(true))
                }
            )

            dataStore.data.mapNotNull {
                it[DEFAULT_SOURCE_LANGUAGE]
            }
                .distinctUntilChanged()
                .onEach {
                    send(SetDefaultSourceLanguage(it))
                }
                .launchIn(this)

            dataStore.data.mapNotNull {
                it[DEFAULT_TARGET_LANGUAGE]
            }
                .distinctUntilChanged()
                .onEach {
                    send(SetDefaultTargetLanguage(it))
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
                    textToTranslate = currentState.textToTranslate,
                    supportedLanguages = currentState.supportedLanguages
                )
                currentState
            }
            TranslationFailure -> {
                // TODO: should probably show & log the error
                currentState
            }
            is TranslationSuccess -> currentState.copy(translatedText = intention.result)
            CopyTextToClipboard -> {
                copyTextToClipboard(currentState.translatedText)
                currentState
            }
            TrySwapLanguages -> {
                return if (currentState.sourceLanguage != LanguageEntity("auto", "Detect")) {
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
        supportedLanguages: List<LanguageEntity>,
        lookUpLanguage: String
    ): LanguageEntity? {
        return supportedLanguages.find { it.name == lookUpLanguage }
    }

    private fun requestTranslation(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String,
        supportedLanguages: List<LanguageEntity>
    ) {
        viewModelScope.launch {
            translate(
                source = sourceLanguageCode,
                target = targetLanguageCode,
                query = textToTranslate
            ).fold(
                success = {
                    send(TranslationSuccess(it.translation))

                    if (sourceLanguageCode == "auto") {
                        it.info?.detectedSource?.let { detectedSourceLanguageCode ->
                            supportedLanguages
                                .find { languageEntity ->
                                    languageEntity.code == detectedSourceLanguageCode
                                }
                                ?.let { detectedSourceLanguage ->
                                    send(SetNewSourceLanguage(detectedSourceLanguage))
                                }
                        }
                    }
                },
                failure = {
                    send(TranslationFailure)
                }
            )
        }
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
        data class OnTextToTranslateChange(val newValue: String) : Intention
        data class DefaultSourceLanguageSelected(val language: LanguageEntity) : Intention
        data class DefaultTargetLanguageSelected(val language: LanguageEntity) : Intention
        data class SetDefaultTargetLanguage(val languageName: String) : Intention
        data class SetDefaultSourceLanguage(val languageName: String) : Intention
        data class SetNewSourceLanguage(val language: LanguageEntity) : Intention
        data class SetNewTargetLanguage(val language: LanguageEntity) : Intention
        data class ShowErrorDialog(val show: Boolean) : Intention
        data class SupportedLanguagesReceived(val languages: List<LanguageEntity>) : Intention
        data class TranslationSuccess(val result: String) : Intention
        object ClearInputField : Intention
        object CopyTextToClipboard : Intention
        object Translate : Intention
        object TranslationFailure : Intention
        object TrySwapLanguages : Intention
        data class ToggleAppTheme(val newTheme: ThemingOptions) : Intention
    }

    sealed interface SideEffect
}
