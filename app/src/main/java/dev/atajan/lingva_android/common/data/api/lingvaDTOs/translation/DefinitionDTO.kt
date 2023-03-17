package dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class DefinitionDTO(
    val list: List<DefinitionListDTO>? = null,
    val type: String? = null,
)