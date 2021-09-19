package dev.atajan.lingva_android.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.atajan.lingva_android.usecases.GetSupportedLanguagesUseCase
import dev.atajan.lingva_android.usecases.GetTranslationUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateScreenViewModel @Inject constructor(
    private val getSupportedLanguages: GetSupportedLanguagesUseCase,
    private val translate: GetTranslationUseCase
) : ViewModel() {

    fun listLanguages() {
        viewModelScope.launch {
            getSupportedLanguages().fold(
                success = {
                    Log.d("y", "${it.languages}")
                },
                failure = {
                    Log.d("y", "getting langs failed with $it")
                }
            )
        }
    }

    fun testTranslate() {
        viewModelScope.launch {
            translate(source = "en", target = "ja", query = "I am an android developer!").fold(
                success = {
                    Log.d("y", it.translation)
                },
                failure = {
                    Log.d("y", "getting langs failed with $it")
                }
            )
        }
    }
}
