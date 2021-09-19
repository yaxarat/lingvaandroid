package dev.atajan.lingva_android.api.entities

import kotlinx.serialization.Serializable

@Serializable
data class LanguagesEntity(
    val languages: List<LanguageEntity>
)
