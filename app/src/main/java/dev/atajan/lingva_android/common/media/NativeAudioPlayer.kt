package dev.atajan.lingva_android.common.media

import android.media.MediaPlayer
import java.io.File
import javax.inject.Inject

class NativeAudioPlayer @Inject constructor() : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var temporaryAudioFile: File? = null

    override fun playAudio(audio: ByteArray) {
        if (mediaPlayer != null) releaseMediaPlayer()
        mediaPlayer = MediaPlayer()
        temporaryAudioFile = createTempAudioFile(audio)

        mediaPlayer?.setOnCompletionListener {
            releaseMediaPlayer()
        }

        temporaryAudioFile?.let { file ->
            mediaPlayer?.apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
        }
    }

    override fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
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