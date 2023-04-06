package dev.atajan.lingva_android.common.usecases.ktorimpl

import dev.atajan.lingva_android.common.data.datasource.AudioRepository
import dev.atajan.lingva_android.common.usecases.ObserveAudioDataUseCase

class KtorObserveAudioDataUseCase(
    private val audioRepository: AudioRepository
) : ObserveAudioDataUseCase {

    override operator fun invoke() = audioRepository.audioRequestResult
}