package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo.Companion.toTranslationWithInfoDomain
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.Failure
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.Loading
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        query: String
    ) {
        launch(Dispatchers.IO) {
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
                Success(translated.toTranslationWithInfoDomain()).emit()
            } catch (error: DTOToDomainModelMappingError) {
                Failure(error.message).emit()
            }
        }
    }

    private fun TranslationRepositoryResponse.emit() {
        translationResult.value = this
    }
}