package dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class InfoDTO(
    val definition: List<DefinitionDTO>? = null,
    val examples: List<String>? = null,
    val extraTranslation: List<ExtraTranslationDTO>? = null,
    val pronunciation: PronunciationDTO? = null,
    val similar: List<String>? = null,
)