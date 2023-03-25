package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.data.api.lingvaDTOs.translation.TranslationDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError
import dev.atajan.lingva_android.common.domain.models.translation.Translation.Companion.toTranslationDomain
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo.Companion.toTranslationWithInfoDomain
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.Failure
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.Loading
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.TranslationSuccess
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.TranslationWithInfoSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class KtorTranslationRepository(
    private val api: KtorLingvaApi,
    applicationScope: CoroutineScope,
) : TranslationRepository, CoroutineScope by applicationScope {

    override val translationResult: MutableStateFlow<TranslationRepositoryResponse> = MutableStateFlow(Loading)

    override fun translate(
        source: String,
        target: String,
        query: String,
        requireInfo: Boolean
    ) {
        launch {
            val translated = try {
                api.translate(
                    source = source,
                    target = target,
                    query = query
                )
            } catch (error: LingvaApiError) {
                Failure(error.message).emit()
                return@launch
            }

            try {
                translated.mapToDomainAndEmit(requireInfo)
            } catch (error: DTOToDomainModelMappingError) {
                Failure(error.message).emit()
            }
        }
    }

    private fun TranslationRepositoryResponse.emit() {
        translationResult.value = this
    }

    private fun TranslationDTO.mapToDomainAndEmit(infoRequired: Boolean) {
        if (infoRequired) {
            TranslationWithInfoSuccess(toTranslationWithInfoDomain())
        } else {
            TranslationSuccess(toTranslationDomain())
        }.emit()
    }
}