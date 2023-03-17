package dev.atajan.lingva_android.common.domain.errors

sealed class DTOToDomainModelMappingError(override val message: String) : Exception() {
    class NullValue(message: String) : DTOToDomainModelMappingError(message)
}

sealed class LingvaApiError(override val message: String) : Exception() {
    object BadEndpoints : LingvaApiError("All endpoints failed")
    object TranslationFailure : LingvaApiError("Error during translation request")
}