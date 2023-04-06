package dev.atajan.lingva_android.common.usecases.ktorimpl

import dev.atajan.lingva_android.common.media.AudioPlayer
import dev.atajan.lingva_android.common.usecases.PlayByteArrayAudioUseCase

class AudioPlayerPlayByteArrayAudioUseCase(
    private val audioPlayer: AudioPlayer
) : PlayByteArrayAudioUseCase {

    override fun invoke(audio: ByteArray) {
        audioPlayer.playAudio(audio)
    }
}