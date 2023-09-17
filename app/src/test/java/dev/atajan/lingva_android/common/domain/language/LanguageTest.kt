package dev.atajan.lingva_android.common.domain.language

import dev.atajan.lingva_android.common.data.api.lingvadto.language.LanguageDTO
import dev.atajan.lingva_android.common.domain.errors.DTOToDomainModelMappingError.NullValue
import dev.atajan.lingva_android.common.domain.models.language.Language
import dev.atajan.lingva_android.common.domain.models.language.Language.Companion.toDomainModel
import dev.atajan.lingva_android.common.domain.models.language.containsLanguageOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class LanguageTest {

    @Test
    fun `when DTO contains null code then throws NullValue exception`() {
        assertThrows("language code can't be null", NullValue::class.java) {
            LanguageDTO(
                code = null,
                name = "English"
            ).toDomainModel()
        }
    }

    @Test
    fun `when DTO contains null name then throws NullValue exception`() {
        assertThrows("language name can't be null", NullValue::class.java) {
            LanguageDTO(
                code = "en",
                name = null
            ).toDomainModel()
        }
    }

    @Test
    fun `containsLanguageOrNull returns language with matching code`() {
        val languages = listOf(
            Language(code = "en", name = "English"),
            Language(code = "jpn", name = "Japanese"),
        )
        val actual = languages.containsLanguageOrNull("jpn")

        assertEquals(languages[1], actual)
    }

    @Test
    fun `containsLanguageOrNull returns null with no matching code`() {
        val languages = listOf(
            Language(code = "en", name = "English"),
            Language(code = "jpn", name = "Japanese"),
        )
        val actual = languages.containsLanguageOrNull("es")

        assertNull(actual)
    }
}