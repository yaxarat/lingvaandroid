package dev.atajan.lingva_android.common.data.api.lingvadto.audio

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AudioDTO(val audio : ArrayList<Int>? = null)
