package dev.atajan.lingva_android.common.domain.models.language

import dev.atajan.lingva_android.common.data.api.lingvadto.language.LanguageDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError.NullValue

data class Language(
    val code: String,
    val name: String,
) {
    companion object {
        fun LanguageDTO.toDomainModel(): Language {
            return Language(
                code = code ?: throw NullValue("language code can't be null"),
                name = name ?: throw NullValue("language name can't be null")
            )
        }
    }
}
