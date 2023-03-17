package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError
import dev.atajan.lingva_android.common.domain.models.language.Language.Companion.toDomainModel
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse.Failure
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse.Loading
import dev.atajan.lingva_android.common.domain.results.LanguagesRepositoryResponse.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class KtorLanguagesRepository(
    private val api: KtorLingvaApi,
    applicationScope: CoroutineScope,
) : LanguagesRepository, CoroutineScope by applicationScope {

    override val supportedLanguages: MutableStateFlow<LanguagesRepositoryResponse> = MutableStateFlow(Loading)

    override fun fetchSupportedLanguages() {
        launch {
            val supportedLanguagesDTO = try {
                api.getSupportedLanguages()
            } catch (error: LingvaApiError) {
                Failure(error.message).emit()
                return@launch
            }

            with(supportedLanguagesDTO) {
                if (languages.isNullOrEmpty()) {
                    Failure("Languages can't be null or empty").emit()
                    return@launch
                }

                languages.let { languages ->
                    try {
                        languages
                            .map { it.toDomainModel() }
                            .let {  Success(it).emit() }
                    } catch (error: DTOToDomainModelMappingError) {
                        Failure(error.message).emit()
                    }
                }
            }
        }
    }

    private fun LanguagesRepositoryResponse.emit() {
        supportedLanguages.value = this
    }
}
