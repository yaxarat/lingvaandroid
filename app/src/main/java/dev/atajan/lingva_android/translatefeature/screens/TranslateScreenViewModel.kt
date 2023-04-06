package dev.atajan.lingva_android.translatefeature.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.common.data.datasource.impl.APP_THEME
import dev.atajan.lingva_android.common.data.datasource.impl.CUSTOM_LINGVA_ENDPOINT
import dev.atajan.lingva_android.common.data.datasource.impl.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.common.data.datasource.impl.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.language.containsLanguageOrNull
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.media.NativeAudioPlayer
import dev.atajan.lingva_android.common.redux.MVIViewModel
import dev.atajan.lingva_android.common.redux.MiddleWare
import dev.atajan.lingva_android.common.redux.stateLogger
import dev.atajan.lingva_android.common.ui.theme.ThemingOptions
import dev.atajan.lingva_android.common.usecases.FetchSupportedLanguagesUseCase
import dev.atajan.lingva_android.common.usecases.ObserveTranslationResultUseCase
import dev.atajan.lingva_android.common.usecases.PlayByteArrayAudioUseCase
import dev.atajan.lingva_android.common.usecases.TranslateUseCase
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.ClearCustomLingvaServerUrl
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
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenIntention.UpdateCustomLingvaServerUrl
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenSideEffect
import dev.atajan.lingva_android.translatefeature.redux.TranslateScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    applicationScope: CoroutineScope,
    translationResult: ObserveTranslationResultUseCase,
    private val clipboardManager: ClipboardManager,
    private val dataStore: DataStore<Preferences>,
    private val supportedLanguages: FetchSupportedLanguagesUseCase,
    private val translate: TranslateUseCase,
    private val playByteArrayAudioUseCase: PlayByteArrayAudioUseCase,
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
        viewModelScope.launch {
            getSupportedLanguages(this)
            observeDefaultLanguages(this)
        }
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
                send(ShowErrorDialog(true))
                currentState
            }
            is TranslationSuccess -> {
                updateSourceLanguageIfNewDetected(
                    currentSourceLanguageCode = currentState.sourceLanguage.code,
                    detectedSourceLanguageCode = intention.translationWithInfo.info.detectedSource
                )
//                val a = intArrayOf(255,243,68,196,0,18,106,181,188,1,67,16,1,174,231,17,18,128,8,42,124,45,19,253,10,144,149,200,202,66,52,231,255,242,103,161,24,224,2,25,78,115,191,231,57,208,231,243,188,239,206,119,32,24,183,33,27,67,156,244,33,25,72,79,255,243,159,144,141,33,63,253,9,146,167,144,132,56,0,3,33,255,128,143,15,120,122,255,45,123,47,19,44,158,95,239,255,243,68,196,9,18,169,206,80,1,154,56,0,221,43,206,210,255,223,248,187,18,134,56,73,128,177,225,159,61,25,132,162,98,177,79,243,207,115,200,57,113,239,207,204,105,118,60,71,83,63,158,254,196,148,240,120,64,214,255,171,183,220,196,52,120,62,100,255,255,120,121,234,171,255,254,190,104,109,12,5,207,101,101,199,27,139,217,45,218,237,167,63,56,48,117,255,243,68,196,17,20,121,186,120,1,204,48,0,167,58,105,9,12,102,207,222,223,99,59,71,251,219,15,38,122,70,38,3,79,160,32,143,211,5,143,33,154,157,152,23,146,204,8,130,25,109,158,217,247,59,33,31,250,203,22,61,239,33,217,62,126,82,179,239,226,165,18,55,99,231,52,179,46,8,135,167,80,230,232,75,220,209,165,152,128,64,109,1,152,129,167,255,243,68,196,18,22,17,182,140,0,65,146,148,163,69,255,242,95,161,17,58,216,234,211,228,201,255,98,114,84,47,85,116,126,96,184,160,72,155,119,107,249,106,237,32,115,106,206,112,124,252,63,215,120,55,114,162,5,118,136,22,237,202,101,16,52,166,177,165,14,206,2,103,194,36,203,138,153,20,119,159,249,74,42,1,212,203,62,198,18,24,66,151,69,4,161,255,243,68,196,12,20,209,186,152,0,26,18,148,112,209,172,121,134,7,240,50,167,74,164,106,153,115,161,126,103,114,117,139,172,71,3,19,1,48,132,46,39,31,70,31,68,201,26,101,236,143,67,71,173,213,235,229,86,87,223,40,62,80,93,56,89,100,214,198,254,193,234,70,155,20,65,71,195,162,188,93,142,253,154,106,190,126,82,68,64,14,228,158,89,85,239,255,243,68,196,11,19,105,138,164,0,122,210,148,207,66,88,145,33,182,15,0,48,52,20,52,144,86,94,235,105,199,245,78,190,216,207,237,245,198,54,187,231,226,93,125,129,1,129,153,160,18,77,38,19,95,153,89,38,35,78,172,205,172,191,225,239,106,252,41,42,188,32,161,168,179,121,7,48,83,85,195,157,251,146,115,92,234,91,251,179,100,65,176,27,9,194,255,243,68,196,16,22,1,166,176,0,203,86,148,149,33,52,13,147,118,209,22,11,51,169,35,34,251,191,37,137,70,187,172,213,213,242,60,28,66,221,134,65,152,7,143,197,6,106,3,145,188,158,108,229,200,37,170,143,57,66,211,239,239,86,227,248,101,113,239,136,231,206,81,137,198,216,81,38,178,175,119,255,255,255,235,118,210,33,160,58,162,198,110,117,36,67,255,243,68,196,11,18,145,58,188,0,156,216,112,33,4,178,28,121,187,57,145,2,26,68,67,164,77,19,12,135,171,78,124,214,181,167,76,229,173,91,254,46,121,166,255,222,76,33,7,97,192,238,142,58,29,54,177,198,182,174,62,179,2,132,15,53,4,73,190,139,5,140,143,178,244,5,213,109,216,248,116,32,113,3,0,184,85,53,28,68,152,9,134,235,49,19,255,243,68,196,19,17,201,66,184,0,147,80,113,209,200,189,212,73,14,243,127,241,8,119,251,53,127,240,122,127,34,162,1,37,40,168,168,224,120,7,128,123,161,97,98,143,32,214,22,22,177,79,116,54,122,43,59,120,172,94,195,139,127,180,237,42,111,81,50,6,21,105,85,1,206,52,83,7,180,18,55,238,98,14,19,239,212,177,228,223,50,61,243,35,71,254,255,243,68,196,30,18,169,174,176,0,163,86,148,210,73,50,42,36,216,18,128,104,112,246,219,15,21,12,230,110,81,68,49,26,239,248,54,63,255,195,99,254,91,17,255,185,55,142,13,8,251,127,255,255,247,88,2,93,239,254,183,113,224,60,9,161,205,210,220,98,108,134,2,145,246,231,8,41,111,212,128,237,255,249,180,95,255,165,35,127,254,110,253,150,216,205,255,243,68,196,38,18,89,54,176,0,196,30,112,157,40,132,53,198,223,230,247,154,180,207,163,98,150,68,140,134,181,157,213,194,203,26,144,23,255,255,255,231,107,2,140,164,53,194,170,156,179,107,238,50,18,55,12,137,155,68,33,168,177,158,66,0,5,19,104,187,254,42,40,185,59,215,165,35,102,46,62,203,74,63,188,85,206,73,75,34,140,206,63,170,116,107,255,243,68,196,47,17,41,54,160,0,195,204,112,126,190,126,249,220,24,145,34,91,255,253,197,127,255,255,255,243,161,39,245,180,181,48,192,248,102,142,18,180,5,69,207,73,33,67,132,96,139,145,57,157,149,219,12,181,151,22,181,53,153,68,61,220,113,238,235,4,20,13,93,254,176,183,148,128,152,8,32,160,212,74,10,130,195,214,183,146,32,18,80,85,110,235,255,243,68,196,61,17,232,190,104,1,91,24,0,234,239,207,127,255,255,255,243,186,89,74,34,4,56,169,131,16,50,122,205,27,112,123,86,70,78,69,162,148,96,6,8,165,183,111,75,210,94,120,235,35,53,18,193,30,18,53,186,105,152,162,163,165,99,14,226,118,155,80,82,7,205,150,194,88,28,194,97,97,157,15,100,234,90,198,65,153,124,196,221,155,243,22,255,243,68,196,72,32,59,42,120,1,154,104,0,82,148,139,148,204,25,72,151,203,63,234,170,154,151,153,186,221,3,133,198,67,255,247,183,67,81,137,185,137,154,141,206,30,48,60,231,191,255,237,86,237,67,82,243,116,139,233,18,133,226,81,144,51,47,157,72,185,153,29,31,80,160,13,96,169,134,134,157,100,193,131,168,12,149,19,39,75,72,239,80,138,10,192,255,243,68,196,26,24,169,218,172,1,154,104,0,151,47,176,241,47,164,10,240,254,104,163,57,243,114,17,132,176,188,98,86,163,115,51,40,246,36,3,156,93,88,218,59,140,129,112,185,166,164,38,200,34,145,130,9,146,108,120,152,102,105,249,170,13,227,189,11,175,251,254,186,15,245,166,122,159,255,215,253,159,255,165,175,82,215,242,92,23,37,221,127,103,66,202,255,243,68,196,10,19,9,54,180,1,217,72,0,22,89,53,161,228,180,51,40,124,100,51,44,13,88,104,172,205,191,114,222,106,88,113,169,40,202,41,120,52,133,151,49,113,84,186,18,33,48,76,3,134,27,50,128,217,143,37,21,205,156,119,110,112,168,48,149,193,18,193,36,153,163,223,202,144,18,85,179,203,3,33,130,150,133,78,36,184,13,40,118,28,73,243,255,243,68,196,16,22,97,54,172,0,206,30,112,137,15,205,102,150,208,253,222,150,202,216,125,23,119,118,93,46,198,87,77,77,220,239,43,219,194,239,114,254,176,161,79,88,174,207,247,203,103,8,180,35,160,189,89,109,240,27,158,94,205,153,140,242,140,154,244,112,131,64,120,58,165,53,197,41,26,179,0,55,205,95,133,222,165,189,250,42,151,57,103,1,211,101,255,243,68,196,9,20,81,134,176,0,203,214,148,213,221,132,59,137,194,199,118,119,130,9,222,140,245,70,170,222,142,182,101,100,250,134,205,108,221,198,180,204,11,118,245,86,98,55,141,139,190,128,217,215,152,142,227,198,108,29,103,78,7,241,249,37,72,134,36,211,180,187,120,153,111,95,207,237,253,237,157,96,105,127,197,21,195,238,149,83,148,216,186,48,123,143,255,243,68,196,10,17,233,6,172,0,206,24,112,39,128,64,49,137,134,197,34,176,53,62,87,44,6,174,53,30,203,124,198,9,179,245,33,25,206,152,118,21,7,222,226,89,50,172,135,224,198,38,208,199,99,229,128,221,74,150,4,130,113,247,33,159,104,192,51,255,251,84,43,255,74,207,84,226,36,209,46,206,140,2,156,234,237,208,240,13,154,206,83,145,106,93,255,243,68,196,21,18,64,234,164,0,206,30,112,164,103,14,20,143,108,242,214,87,86,156,61,133,11,231,59,172,38,92,122,144,151,246,241,111,22,149,107,40,85,146,88,169,1,208,142,158,1,123,115,123,1,104,68,33,59,255,255,52,207,241,232,185,184,104,194,80,121,42,89,120,152,114,8,100,84,99,244,119,154,202,249,30,149,12,224,136,237,94,206,45,75,12,255,243,68,196,31,17,208,234,156,0,206,48,112,186,156,24,233,177,177,218,186,101,16,245,88,36,170,70,109,76,203,199,131,33,218,182,68,55,4,174,235,211,84,209,235,125,221,110,255,255,245,255,250,234,135,101,238,129,139,152,95,8,218,126,154,224,10,241,230,50,72,13,234,6,45,96,129,76,74,91,56,58,69,57,179,31,82,221,226,245,53,157,80,21,6,230,255,243,68,196,42,18,160,210,144,0,214,48,112,71,88,65,16,164,81,65,0,66,19,2,217,165,113,115,226,85,67,182,102,211,166,91,171,128,176,54,228,255,255,176,121,26,107,148,9,146,101,178,161,130,46,232,32,16,209,150,216,236,168,193,73,1,208,107,48,128,66,26,183,91,32,172,209,158,172,79,20,246,87,68,124,23,137,53,44,114,197,94,85,163,113,68,255,243,68,196,50,18,136,222,132,0,219,222,112,204,152,138,75,113,163,113,7,14,59,113,209,22,8,170,223,255,244,72,3,228,157,255,244,213,127,95,69,15,54,208,78,168,85,136,21,16,122,89,32,234,90,12,28,5,171,83,57,177,0,215,165,153,74,67,60,150,38,100,57,101,156,179,31,175,2,101,25,2,1,52,99,41,82,68,72,239,81,163,162,98,115,36,255,243,68,196,58,18,40,242,132,0,214,18,112,161,145,32,153,195,163,76,174,241,56,123,255,241,201,66,142,110,96,64,144,38,155,142,176,101,136,230,200,130,87,3,4,7,29,62,180,104,241,72,97,130,70,33,247,161,180,189,201,93,188,245,43,183,253,149,99,104,193,198,215,175,121,202,84,174,110,103,121,206,166,252,176,190,241,175,50,154,94,41,3,136,36,191,255,243,68,196,68,17,129,6,144,0,214,24,112,234,20,203,244,147,197,123,134,156,167,104,51,170,153,83,10,172,245,6,45,46,116,170,229,239,179,153,204,106,209,254,92,145,247,247,99,250,116,114,147,213,89,17,102,224,152,72,219,133,24,142,212,65,146,85,28,228,133,187,88,187,76,100,81,159,40,112,164,189,74,82,216,159,253,85,199,240,145,148,254,142,234,141,255,243,68,196,81,18,25,42,172,0,198,18,112,33,38,252,93,248,3,96,28,128,29,56,35,226,163,24,11,113,107,38,77,243,18,190,165,190,27,251,191,82,182,148,220,184,82,56,246,147,128,48,118,48,208,117,154,121,62,113,228,132,56,124,172,120,57,66,161,240,140,88,250,154,177,2,133,6,6,233,255,255,255,255,253,10,203,117,8,70,24,107,116,143,184,251,255,243,68,196,91,20,25,54,172,0,197,22,112,167,42,64,96,110,64,19,168,38,36,38,170,58,41,229,92,204,173,166,147,235,254,239,230,190,43,223,251,135,217,106,96,120,146,129,195,81,246,228,63,155,110,36,23,186,146,33,214,166,44,231,146,134,98,104,69,197,220,65,250,191,103,92,175,255,255,244,211,181,250,29,50,76,19,27,2,50,43,18,25,107,8,186,255,243,68,196,93,19,121,70,172,0,197,22,112,54,134,25,200,233,81,245,18,1,229,108,229,143,46,18,39,144,64,243,233,163,82,218,131,35,93,235,172,67,197,95,194,55,84,55,37,229,80,155,22,243,109,26,162,128,159,92,31,208,22,220,83,207,152,224,98,149,212,45,238,152,165,233,185,237,91,111,116,241,164,168,84,160,4,87,235,10,251,71,11,255,255,221,255,243,68,196,98,25,129,154,164,0,211,94,148,103,96,105,168,179,200,12,101,186,176,222,135,140,49,86,91,5,169,153,145,194,17,23,37,206,117,105,188,91,11,170,207,145,91,93,177,190,142,254,52,93,125,214,247,180,42,106,213,249,197,169,22,52,247,165,116,192,185,83,21,229,140,42,31,147,118,116,1,118,83,30,174,211,247,57,244,180,174,117,136,178,194,196,255,243,68,196,79,28,193,158,156,0,211,222,148,240,181,37,163,230,251,182,164,220,254,54,35,195,133,32,48,111,65,187,74,155,22,102,209,19,89,255,253,247,148,159,123,203,5,239,20,207,24,144,9,195,231,21,110,132,85,160,244,64,54,17,13,152,38,158,221,81,18,31,32,117,125,1,91,23,85,141,224,52,111,84,223,247,182,41,188,110,250,205,47,93,95,89,255,243,68,196,47,26,233,162,152,0,211,222,148,135,2,44,85,246,5,82,188,226,37,38,233,32,60,209,169,67,245,33,182,183,208,220,17,170,233,36,142,234,44,45,214,158,39,173,62,171,76,124,127,95,169,54,96,53,111,114,168,230,22,26,79,255,252,226,176,9,128,163,31,82,169,140,172,122,157,235,240,72,151,167,249,1,71,61,139,2,125,85,180,104,76,56,255,243,68,196,22,22,209,154,148,0,210,22,148,24,58,1,207,113,9,177,69,203,59,239,221,175,182,233,174,45,219,37,136,30,60,224,248,172,2,226,99,65,76,13,143,80,72,55,48,29,227,164,118,217,195,182,245,249,219,247,196,54,57,173,71,116,173,11,157,79,198,126,179,212,127,255,127,20,70,85,0,58,106,222,160,16,51,72,117,226,48,77,9,131,73,134,255,243,68,196,13,18,65,158,152,0,209,78,148,75,128,199,70,230,16,67,45,141,5,68,15,225,21,141,21,199,126,218,104,217,183,106,156,232,173,16,141,148,71,22,30,39,23,76,112,122,15,135,6,172,101,222,198,183,79,223,70,71,43,71,251,254,179,168,255,255,95,215,155,194,186,141,5,83,169,56,111,188,132,45,52,169,120,27,104,164,92,185,83,204,52,87,255,243,68,196,23,18,129,158,144,0,201,80,148,6,198,60,173,33,243,118,215,235,250,249,251,57,216,128,248,20,7,96,212,120,168,45,48,145,81,49,164,54,197,79,247,241,223,207,235,194,212,33,214,113,127,167,255,255,255,250,198,137,79,17,66,67,184,240,27,107,55,4,192,58,61,38,69,174,135,26,68,243,146,20,96,153,52,98,85,26,14,186,58,77,73,254,255,243,68,196,32,16,177,50,132,0,203,78,112,182,255,253,232,121,231,141,84,104,3,132,81,163,176,220,104,37,13,135,201,34,148,3,44,232,7,250,208,207,255,238,255,255,255,166,6,24,118,176,110,140,5,36,20,168,5,97,108,130,18,137,24,43,2,131,50,1,83,85,53,148,95,166,191,218,215,255,156,203,253,28,191,203,215,153,245,43,26,142,225,208,4,5,255,243,68,196,48,18,169,38,92,1,89,40,0,21,50,141,21,18,15,56,173,12,34,34,44,171,113,19,250,2,112,104,123,191,255,255,255,250,169,85,51,102,20,188,6,81,60,78,1,151,184,104,140,69,203,129,151,131,98,149,106,155,23,14,146,166,78,56,69,133,0,230,168,138,123,128,80,23,68,48,182,135,189,25,174,225,78,56,78,11,4,136,183,209,153,108,255,243,68,196,56,31,115,34,80,1,154,80,1,5,64,95,46,5,113,4,20,141,102,246,157,193,160,106,55,27,136,177,97,110,221,235,67,46,230,42,130,225,8,132,17,196,224,182,62,2,192,44,111,111,209,155,183,212,69,138,244,38,48,193,161,57,228,125,118,223,255,57,63,94,230,33,138,88,247,46,204,72,141,58,217,197,137,27,7,87,69,166,78,108,229,86,255,243,68,196,13,20,74,34,152,1,143,40,0,230,162,127,251,7,195,230,65,188,112,215,28,166,167,185,220,62,122,160,241,159,28,81,228,56,124,202,230,19,14,125,3,133,70,67,161,24,122,167,255,102,118,69,38,36,52,134,96,152,123,254,196,165,25,155,104,56,244,24,181,132,86,75,255,211,114,77,72,170,31,131,89,107,31,91,14,188,233,105,114,199,163,150,255,243,68,196,14,18,177,170,8,1,203,40,0,62,191,154,97,98,59,109,179,230,53,220,164,48,177,1,145,25,80,162,205,69,118,216,123,25,220,72,226,74,110,99,101,105,144,168,233,250,80,200,44,34,88,69,10,237,50,250,22,203,73,122,88,146,38,189,22,208,119,101,188,94,22,4,20,48,130,4,76,9,0,4,32,64,130,0,155,194,0,128,32,15,131,228,255,243,68,196,22,16,224,69,236,0,72,204,36,254,60,16,8,12,64,161,162,0,128,32,126,198,4,210,76,251,253,92,122,42,70,64,16,12,113,128,48,124,31,168,31,7,195,225,113,213,137,195,227,62,143,255,4,1,10,7,57,11,194,162,44,120,83,161,101,129,66,93,203,123,248,160,96,250,121,102,137,3,53,67,21,190,48,176,115,220,240,2,7,147,38,76,255,243,68,196,37,27,194,25,236,0,121,134,153,157,177,238,128,64,197,43,59,222,40,48,97,84,72,166,55,33,130,131,81,197,139,22,127,242,73,60,150,98,197,189,170,176,218,25,194,86,20,24,19,52,75,36,49,217,152,48,119,248,16,20,167,93,142,127,199,213,255,116,81,210,20,102,215,199,31,2,21,200,38,149,248,59,232,22,4,198,16,155,98,50,34,238,255,243,68,196,9,16,144,202,0,0,73,134,112,70,128,45,101,145,57,33,75,169,124,255,53,182,138,237,15,95,52,74,200,56,169,84,141,66,136,55,5,77,173,6,133,216,104,208,179,30,223,90,24,58,238,101,152,77,218,163,118,164,81,62,53,13,87,60,55,250,42,2,196,241,212,148,157,16,21,24,4,48,113,133,5,130,194,161,144,88,148,42,25,9,1,73,255,243,68,196,25,15,240,65,212,0,96,198,36,62,84,40,5,17,153,173,33,32,40,80,215,164,103,226,196,130,188,122,66,69,137,15,218,5,34,51,253,124,123,7,167,90,90,255,83,29,248,74,76,65,77,69,51,46,49,48,48,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,76,65,77,69,51,46,255,243,68,196,44,0,0,3,72,0,0,0,0,49,48,48,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,76,65,77,69,51,46,255,243,68,196,127,0,0,3,72,0,0,0,0,49,48,48,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,255,243,68,196,172,0,0,3,72,0,0,0,0,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,255,243,68,196,172,0,0,3,72,0,0,0,0,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170,170)
//                val byteArray = a.map { it.toByte() }.toByteArray()
//                playByteArrayAudioUseCase(byteArray)

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
            ClearCustomLingvaServerUrl -> {
                updateCustomLingvaServer("")
                currentState
            }
            is UpdateCustomLingvaServerUrl -> {
                updateCustomLingvaServer(intention.url)
                currentState
            }
        }
    }

    fun onTextToTranslateChange(newValue: String) {
        textToTranslate = newValue
    }

    private fun updateCustomLingvaServer(url: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[CUSTOM_LINGVA_ENDPOINT] = url
            }
        }
    }

    private fun getSupportedLanguages(scope: CoroutineScope) {
        scope.launch {
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
}
