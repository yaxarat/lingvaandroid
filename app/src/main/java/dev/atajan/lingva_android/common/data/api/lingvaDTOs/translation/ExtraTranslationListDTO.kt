package dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ExtraTranslationListDTO(
    val frequency: Int? = null,
    val meanings: List<String>? = null,
    val word: String? = null,
)