package dev.atajan.lingva_android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
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
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.Intention
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.SideEffect
import dev.atajan.lingva_android.ui.screens.TranslateScreenViewModel.State
import dev.atajan.lingva_android.ui.theme.ThemingOptions
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.GetTranslationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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

    override fun reduce(
        currentState: State,
        intention: Intention,
        middleWares: List<MiddleWare<State, Intention>>
    ): State {
        return when (intention) {
            Intention.FetchSupportedLanguages -> {
                fetchSupportedLanguages()
                currentState
            }
            is Intention.ShowErrorDialog -> currentState.copy(errorDialogState = intention.show)
            is Intention.SupportedLanguagesReceived -> {
                send(Intention.SetDefaultLanguages)
                currentState.copy(supportedLanguages = intention.languages)
            }
            Intention.SetDefaultLanguages -> {
                val defaultSourceLanguage = getDefaultSourceLanguageIfProvided(currentState.supportedLanguages)
                val defaultTargetLanguage = getDefaultTargetLanguageIfProvided(currentState.supportedLanguages)

                currentState.copy(
                    sourceLanguage = defaultSourceLanguage ?: currentState.sourceLanguage,
                    defaultSourceLanguage = defaultSourceLanguage?.name ?: currentState.defaultSourceLanguage,
                    targetLanguage = defaultTargetLanguage ?: currentState.targetLanguage,
                    defaultTargetLanguage = defaultTargetLanguage?.name ?: currentState.defaultTargetLanguage
                )
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
        }
    }

    private fun fetchSupportedLanguages() {
        viewModelScope.launch {
            getSupportedLanguages().fold(
                success = {
                    send(Intention.SupportedLanguagesReceived(it.languages))
                },
                failure = {
                    send(Intention.ShowErrorDialog(true))
                }
            )
        }
    }

    private fun getDefaultSourceLanguageIfProvided(supportedLanguages: List<LanguageEntity>): LanguageEntity? {
        var defaultSourceLanguage: LanguageEntity? = null

        getDefaultSourceLanguageOrNull()
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { defaultSourceLanguageName ->
                supportedLanguages
                    .find { it.name == defaultSourceLanguageName }
                    ?.let { defaultSourceLanguage = it }
            }

        return defaultSourceLanguage
    }

    private fun getDefaultTargetLanguageIfProvided(supportedLanguages: List<LanguageEntity>): LanguageEntity? {
        var defaultTargetLanguage: LanguageEntity? = null

        getDefaultTargetLanguageOrNull()
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { defaultTargetLanguageName ->
                supportedLanguages
                    .find { it.name == defaultTargetLanguageName }
                    ?.let { defaultTargetLanguage = it }
            }

        return defaultTargetLanguage
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

    fun toggleAppTheme(newTheme: ThemingOptions) {
        viewModelScope.launch {
            application.applicationContext.dataStore.edit { preferences ->
                preferences[APP_THEME] = newTheme.name
            }
        }
    }

    fun setDefaultSourceLanguage(newLanguage: LanguageEntity) {
        viewModelScope.launch {
            application.applicationContext.dataStore.edit { preferences ->
                preferences[DEFAULT_SOURCE_LANGUAGE] = newLanguage.name
            }
        }
    }

    fun setDefaultTargetLanguage(newLanguage: LanguageEntity) {
        viewModelScope.launch {
            application.applicationContext.dataStore.edit { preferences ->
                preferences[DEFAULT_TARGET_LANGUAGE] = newLanguage.name
            }
        }
    }

    private fun getDefaultSourceLanguageOrNull(): Flow<String?> {
        return application.applicationContext.dataStore.data.map { preferences ->
            preferences[DEFAULT_SOURCE_LANGUAGE]
        }
    }

    private fun getDefaultTargetLanguageOrNull(): Flow<String?> {
        return application.applicationContext.dataStore.data.map { preferences ->
            preferences[DEFAULT_TARGET_LANGUAGE]
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

    sealed class Intention {
        class OnTextToTranslateChange(val newValue: String) : Intention()
        class SetNewSourceLanguage(val language: LanguageEntity) : Intention()
        class SetNewTargetLanguage(val language: LanguageEntity) : Intention()
        class ShowErrorDialog(val show: Boolean) : Intention()
        class SupportedLanguagesReceived(val languages: List<LanguageEntity>) : Intention()
        class TranslationSuccess(val result: String) : Intention()
        object CopyTextToClipboard : Intention()
        object FetchSupportedLanguages : Intention()
        object SetDefaultLanguages : Intention()
        object Translate : Intention()
        object TranslationFailure : Intention()
        object TrySwapLanguages : Intention()
        object ClearInputField : Intention()
    }

    sealed class SideEffect
}
