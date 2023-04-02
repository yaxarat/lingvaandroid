package dev.atajan.lingva_android.common.domain.models.translation

import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError.NullValue
import dev.atajan.lingva_android.common.domain.models.translation.Translation.Companion.toTranslationDomain
import dev.atajan.lingva_android.common.domain.models.translation.TranslationInfo.Companion.toTranslationInfoDomain

data class TranslationWithInfo(
    val translation: Translation,
    val info: TranslationInfo
) {
    companion object {
        fun TranslationDTO.toTranslationWithInfoDomain() : TranslationWithInfo {
            return TranslationWithInfo(
                translation = toTranslationDomain(),
                info = info?.toTranslationInfoDomain() ?: throw NullValue("translation info can't be null")
            )
        }

        fun TranslationWithInfo.toTranslation() : Translation {
            return Translation(this.translation.result)
        }
    }
}
