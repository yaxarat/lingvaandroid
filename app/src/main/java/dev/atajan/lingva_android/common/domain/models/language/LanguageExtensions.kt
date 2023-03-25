package dev.atajan.lingva_android.common.domain.models.language

internal fun List<Language>.containsLanguageOrNull(languageCode: String): Language? {
    return this.find { it.code == languageCode }
}