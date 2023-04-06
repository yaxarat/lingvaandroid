package dev.atajan.lingva_android.common.usecases

interface RequestAudioDataUseCase {

        operator fun invoke(
            language: String,
            query: String
        )
}