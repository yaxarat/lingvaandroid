package dev.atajan.lingva_android.common.data.api.lingvaDTOs.language

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LanguageDTO(
    val code: String? = null,
    val name: String? = null,
)