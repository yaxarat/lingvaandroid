package dev.atajan.lingva_android.common.media

interface AudioPlayer {

    // Play audio from a ByteArray
    fun playAudio(audio: ByteArray)

    // Release the MediaPlayer
    fun releaseMediaPlayer()
}