package dev.atajan.lingva_android.common.data.datasource.impl

import dev.atajan.lingva_android.common.data.api.KtorLingvaApi
import dev.atajan.lingva_android.common.data.datasource.AudioRepository
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError
import dev.atajan.lingva_android.common.domain.errors.LingvaApiError
import dev.atajan.lingva_android.common.domain.models.audio.Audio.Companion.toAudioDomain
import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse
import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse.Failure
import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class KtorAudioRepository(
    private val api: KtorLingvaApi,
    applicationScope: CoroutineScope,
) : AudioRepository, CoroutineScope by applicationScope {

    override val audioRequestResult: MutableSharedFlow<AudioRepositoryResponse> = MutableSharedFlow()

    override fun requestAudio(
        language: String,
        query: String
    ) {
        launch(Dispatchers.IO) {
            val rawAudio = try {
                api.getAudio(
                    language = language,
                    query = query
                )
            } catch (error: LingvaApiError) {
                Failure(error.message).emit()
                return@launch
            }

            try {
                Success(rawAudio.toAudioDomain()).emit()
            } catch (error: DTOToDomainModelMappingError) {
                Failure(error.message).emit()
            }
        }
    }

    private suspend fun AudioRepositoryResponse.emit() {
        audioRequestResult.emit(this)
    }
}