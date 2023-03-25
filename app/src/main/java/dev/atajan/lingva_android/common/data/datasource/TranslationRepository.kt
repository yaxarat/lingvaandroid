package dev.atajan.lingva_android.common.data.datasource

import dev.atajan.lingva_android.common.domain.models.translation.Translation
import dev.atajan.lingva_android.common.domain.models.translation.TranslationWithInfo
import dev.atajan.lingva_android.common.domain.results.TranslationRepositoryResponse
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {

    /**
     * Emits translation result as either [Translation] or [TranslationWithInfo] depending on
     * whether [translate] has requireInfo set to true or not.
     */
    val translationResult: Flow<TranslationRepositoryResponse>

    /**
     * Translates the [query] from [source] to [target]. [requireInfo] determines whether the translation
     * result should be emitted as [Translation] or [TranslationWithInfo].
     */
    fun translate(
        source: String,
        target: String,
        query: String,
        requireInfo: Boolean = false
    )
}