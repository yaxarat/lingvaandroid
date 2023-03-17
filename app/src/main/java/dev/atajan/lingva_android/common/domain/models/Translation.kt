package dev.atajan.lingva_android.common.domain.models

import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError.NullValue

data class Translation(val translation: String) {
    companion object {
        fun TranslationDTO.toDomainModel() : Translation {
            return Translation(
                translation = translation ?: throw NullValue("translation can't be null")
            )
        }
    }
}
