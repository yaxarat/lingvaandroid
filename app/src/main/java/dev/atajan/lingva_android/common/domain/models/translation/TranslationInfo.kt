package dev.atajan.lingva_android.common.domain.models.translation

import dev.atajan.lingva_android.common.data.api.lingvadto.translation.InfoDTO

/**
 * Although the API returns more information about the translation,
 * only subset of it is used for the app features at the moment.
 *
 * See [InfoDTO] for other fields returned by the API.
 */
data class TranslationInfo(
    val detectedSource: String,
    val pronunciation: String,
) {
    companion object {
        fun InfoDTO.toTranslationInfoDomain() : TranslationInfo {
            return TranslationInfo(
                detectedSource = detectedSource ?: "",
                pronunciation = pronunciation?.translation ?: ""
            )
        }
    }
}
