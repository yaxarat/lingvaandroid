package dev.atajan.lingva_android.ui.screens

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.api.entities.LanguageEntity
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.GetTranslationUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    private val getSupportedLanguages: GetSupportedLanguagesUseCase,
    private val translate: GetTranslationUseCase
) : ViewModel() {

    val supportedLanguages = mutableStateOf(emptyList<LanguageEntity>())
    val translatedText = mutableStateOf("")
    val sourceLanguage = mutableStateOf(LanguageEntity("auto", "Detect"))
    val targetLanguage = mutableStateOf(LanguageEntity("es", "Spanish"))
    val textToTranslate = mutableStateOf("")

    init {
        viewModelScope.launch {
            getSupportedLanguages().fold(
                success = {
                    Log.d("y", "${it.languages}")
                    supportedLanguages.value = it.languages
                },
                failure = {
                    Log.d("y", "getting langs failed with $it")
                }
            )
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
                    Log.d("y", it.translation)
                    translatedText.value = it.translation
                },
                failure = {
                    Log.d("y", "getting langs failed with $it")
                }
            )
        }
    }
}
