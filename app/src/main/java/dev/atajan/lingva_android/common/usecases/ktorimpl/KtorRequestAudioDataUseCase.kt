package dev.atajan.lingva_android.common.usecases.ktorimpl

import dev.atajan.lingva_android.common.data.datasource.AudioRepository
import dev.atajan.lingva_android.common.usecases.RequestAudioDataUseCase

class KtorRequestAudioDataUseCase(
    private val audioRepository: AudioRepository
) : RequestAudioDataUseCase {

    override operator fun invoke(
        language: String,
        query: String
    ) {
        audioRepository.requestAudio(
            language = language,
            query = query
        )
    }
}