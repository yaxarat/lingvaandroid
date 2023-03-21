package dev.atajan.lingva_android.common.usecases

interface TranslateUseCase {

    /**
     * Translates a [textToTranslate] from [sourceLanguageCode] language to [targetLanguageCode] language.
     * The result must be observed via [ObserveTranslationResultUseCase].
     */
    operator fun invoke(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        textToTranslate: String
    )
}
