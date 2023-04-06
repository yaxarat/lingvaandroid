package dev.atajan.lingva_android.common.data.api.lingvadto.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class TranslationDTO(
    val info: InfoDTO? = null,
    val translation: String? = null,
)