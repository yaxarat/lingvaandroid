package dev.atajan.lingva_android.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.atajan.lingva_android.common.data.api.entities.LanguageEntity
import dev.atajan.lingva_android.common.data.datastore.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.data.datastore.dataStore
import dev.atajan.lingva_android.common.mvi.MVIViewModel
import dev.atajan.lingva_android.common.mvi.MiddleWare
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.CopyTextToClipboard
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.OnTextToTranslateChange
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.SetDefaultTargetLanguage
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.SetNewSourceLanguage
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.SetNewTargetLanguage
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.ShowErrorDialog
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.SupportedLanguagesReceived
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.Translate
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.TranslationFailure
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.Intention.TranslationSuccess
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.SideEffect
import dev.atajan.lingva_android.ui.screens.QuickTranslateScreenViewModel.State
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
class QuickTranslateScreenViewModel @Inject constructor(
    @ApplicationContext application: Context,
    applicationScope: CoroutineScope,
    private val getSupportedLanguages: GetSupportedLanguagesUseCase,
    private val translate: GetTranslationUseCase,
) : MVIViewModel<State, Intention, SideEffect>(
    scope = applicationScope,
    initialState = State()
) {
    private val dataStore = application.applicationContext.dataStore
    private val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    init {
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
                it[DEFAULT_TARGET_LANGUAGE]
            }
                .distinctUntilChanged()
                .onEach {
                    send(SetDefaultTargetLanguage(it))
                }
                .launchIn(this)

            send(Translate)
        }
    }

    override fun reduce(
        currentState: State,
        intention: Intention,
        middleWares: List<MiddleWare<State, Intention>>
    ): State {
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
                                    send(
                                        SetNewSourceLanguage(
                                            detectedSourceLanguage
                                        )
                                    )
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

    private fun getDefaultLanguageIfProvided(
        supportedLanguages: List<LanguageEntity>,
        lookUpLanguage: String
    ): LanguageEntity? {
        return supportedLanguages.find { it.name == lookUpLanguage }
    }


    data class State(
        val supportedLanguages: List<LanguageEntity> = emptyList(),
        val translatedText: String = "",
        val sourceLanguage: LanguageEntity = LanguageEntity("auto", "Detect"),
        val targetLanguage: LanguageEntity = LanguageEntity("es", "Spanish"),
        val textToTranslate: String = "",
        val errorDialogState: Boolean = false,
        val defaultTargetLanguage: String = ""
    )

    sealed interface Intention {
        data class OnTextToTranslateChange(val newValue: String) : Intention
        data class SetDefaultTargetLanguage(val languageName: String) : Intention
        data class SetNewSourceLanguage(val language: LanguageEntity) : Intention
        data class SetNewTargetLanguage(val language: LanguageEntity) : Intention
        data class ShowErrorDialog(val show: Boolean) : Intention
        data class SupportedLanguagesReceived(val languages: List<LanguageEntity>) : Intention
        data class TranslationSuccess(val result: String) : Intention
        object CopyTextToClipboard : Intention
        object Translate : Intention
        object TranslationFailure : Intention
    }
    sealed interface SideEffect
}