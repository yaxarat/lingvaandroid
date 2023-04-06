package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface AudioRepository {

    val audioRequestResult: Flow<AudioRepositoryResponse>

    fun requestAudio(
        language: String,
        query: String
    )
}