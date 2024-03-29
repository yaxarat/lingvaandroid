package dev.atajan.lingva_android.common.data.api.lingvadto.language

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LanguagesDTO(
    val languages: List<LanguageDTO>? = null,
)