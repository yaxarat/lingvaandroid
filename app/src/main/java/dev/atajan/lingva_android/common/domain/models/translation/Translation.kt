package dev.atajan.lingva_android.common.domain.models.translation

import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError.NullValue

data class Translation(val result: String) {
    companion object {
        fun TranslationDTO.toTranslationDomain() : Translation {
            return Translation(
                result = translation ?: throw NullValue("translation can't be null")
            )
        }
    }
}
