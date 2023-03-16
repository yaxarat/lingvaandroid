package dev.atajan.lingva_android.common.data.api.entities

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class TranslationInfoEntity(
    val detectedSource: String? = null
)
