package dev.atajan.lingva_android.common.usecases

interface PlayByteArrayAudioUseCase {
    operator fun invoke(audio: ByteArray)
}