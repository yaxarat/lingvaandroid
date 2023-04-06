package dev.atajan.lingva_android.common.usecases

import dev.atajan.lingva_android.common.domain.results.AudioRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface ObserveAudioDataUseCase {
        operator fun invoke(): Flow<AudioRepositoryResponse>
}