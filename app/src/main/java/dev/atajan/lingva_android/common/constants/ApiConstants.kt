package dev.atajan.lingva_android.common.constants

internal const val LINGVA = "https://lingva.ml/api/v1/"
internal const val PLAUSIBILITY = "https://translate.plausibility.cloud/api/v1/"
internal const val PROJECTSEGFAU = "https://translate.projectsegfau.lt/api/v1/"
internal const val DR460NF1R3 = "https://translate.dr460nf1r3.org/api/v1/"
internal const val GARUDALINUX = "https://lingva.garudalinux.org/api/v1/"

internal val TRANSLATION_PROVIDERS by lazy {
    listOf(
        LINGVA,
        PLAUSIBILITY,
        PROJECTSEGFAU,
        DR460NF1R3,
        GARUDALINUX
    )
}