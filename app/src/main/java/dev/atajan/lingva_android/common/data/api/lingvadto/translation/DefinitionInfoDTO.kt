package dev.atajan.lingva_android.common.data.api.lingvadto.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class DefinitionInfoDTO(
    val definition: String? = null,
    val example: String? = null,
    val synonyms: List<String>? = null,
)