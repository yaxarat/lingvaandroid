package dev.atajan.lingva_android.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class LanguageEntity(
    val code: String,
    val name: String
)

