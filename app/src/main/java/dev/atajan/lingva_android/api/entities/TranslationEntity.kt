package dev.atajan.lingva_android.api.entities

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class TranslationEntity(
    val translation: String,
    val info: TranslationInfoEntity? = null
)
