package dev.atajan.lingva_android.ui.screens

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.MainApplication
import dev.atajan.lingva_android.api.entities.LanguageEntity
import dev.atajan.lingva_android.datastore.APP_THEME
import dev.atajan.lingva_android.datastore.DEFAULT_SOURCE_LANGUAGE
import dev.atajan.lingva_android.datastore.DEFAULT_TARGET_LANGUAGE
import dev.atajan.lingva_android.datastore.dataStore
import dev.atajan.lingva_android.ui.theme.ThemingOptions
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.GetTranslationUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    private val application: MainApplication,
    private val getSupportedLanguages: GetSupportedLanguagesUseCase,
    private val translate: GetTranslationUseCase
) : ViewModel() {

    val supportedLanguages = mutableStateOf(emptyList<LanguageEntity>())
    val translatedText = mutableStateOf("")
    val sourceLanguage = mutableStateOf(LanguageEntity("auto", "Detect"))
    val targetLanguage = mutableStateOf(LanguageEntity("es", "Spanish"))
    val textToTranslate = mutableStateOf("")

    var defaultSourceLanguage = mutableStateOf("")
        private set

    var defaultTargetLanguage = mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            getSupportedLanguages().fold(
                success = {
                    Log.d("${this::class}", "${it.languages}")
                    supportedLanguages.value = it.languages
                },
                failure = {
                    Log.d("${this::class}", "getting langs failed with $it")
                    return@launch // No point in going further since supportedLanguages will be empty
                }
            )

            getDefaultSourceLanguageOrNull()
                .filterNotNull()
                .distinctUntilChanged()
                .onEach { defaultSourceLanguageName ->
                    supportedLanguages.value
                        .find { languageEntity ->
                            languageEntity.name == defaultSourceLanguageName
                        }
                        ?.let {
                            sourceLanguage.value = it
                            defaultSourceLanguage.value = it.name
                        }
                }
                .launchIn(this)

            getDefaultTargetLanguageOrNull()
                .filterNotNull()
                .distinctUntilChanged()
                .onEach { defaultTargetLanguageName ->
                    supportedLanguages.value
                        .find { languageEntity ->
                            languageEntity.name == defaultTargetLanguageName
                        }
                        ?.let {
                            targetLanguage.value = it
                            defaultTargetLanguage.value = it.name
                        }
                }
                .launchIn(this)
        }
    }

    fun translate() {
        viewModelScope.launch {
            translate(
                source = sourceLanguage.value.code,
                target = targetLanguage.value.code,
                query = textToTranslate.value
            ).fold(
                success = {
                    Log.d("${this::class}", it.translation)
                    translatedText.value = it.translation
                },
                failure = {
                    Log.d("${this::class}", "getting langs failed with $it")
                }
            )
        }
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
}
