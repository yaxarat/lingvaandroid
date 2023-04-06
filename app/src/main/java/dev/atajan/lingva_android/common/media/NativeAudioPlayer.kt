package dev.atajan.lingva_android.common.media

import android.media.MediaPlayer
import java.io.File
import javax.inject.Inject

class NativeAudioPlayer @Inject constructor() : AudioPlayer {

    private val mediaPlayer = MediaPlayer()

    private var temporaryAudioFile: File? = null

    init {
        mediaPlayer.setOnCompletionListener {
            releaseMediaPlayer()
        }
    }

    override fun playAudio(audio: ByteArray) {
        temporaryAudioFile = createTempAudioFile(audio)
            .also {
                with(mediaPlayer) {
                    setDataSource(it.absolutePath)
                    prepare()
                    start()
                }
            }
    }

    override fun releaseMediaPlayer() {
        mediaPlayer.release()
        temporaryAudioFile?.delete()
        temporaryAudioFile = null
    }

    private fun createTempAudioFile(audio: ByteArray): File {
        return File
            .createTempFile(
                /* prefix = */ "temp_audio",
                /* suffix = */ ".mp3"
            )
            .apply { writeBytes(audio) }
    }
}