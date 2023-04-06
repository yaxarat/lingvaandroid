package dev.atajan.lingva_android.common.domain.models.audio

import dev.atajan.lingva_android.common.data.api.lingvadto.audio.AudioDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError.NullValue

data class Audio(val audioByteArray: ByteArray) {

    companion object {
        fun AudioDTO.toAudioDomain() : Audio {
            return Audio(
                audio
                    ?.map { it.toByte() }
                    ?.toByteArray() ?: throw NullValue("audio can't be null")
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Audio

        if (!audioByteArray.contentEquals(other.audioByteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        return audioByteArray.contentHashCode()
    }
}
