package dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PronunciationDTO(
    val query: String? = null,
    val translation: String? = null
)