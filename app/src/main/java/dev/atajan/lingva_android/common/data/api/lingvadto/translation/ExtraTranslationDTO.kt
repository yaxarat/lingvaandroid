package dev.atajan.lingva_android.common.data.api.lingvadto.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ExtraTranslationDTO(
    val list: List<ExtraTranslationListDTO>? = null,
    val type: String? = null,
)