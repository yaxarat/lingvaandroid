package dev.atajan.lingva_android.usecases

interface TranslateWithInfoUseCase {

    /**
     * Translates a [textToTranslate] from [sourceLanguageCode] language to [targetLanguageCode] language.
     * The result must be observed via [ObserveTranslationResultUseCase].
     */
    operator fun invoke(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String,
    )
}
