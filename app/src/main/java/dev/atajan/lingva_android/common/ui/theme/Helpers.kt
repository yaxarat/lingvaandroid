package dev.atajan.lingva_android.common.ui.theme

import android.content.Context
import dev.atajan.lingva_android.common.data.datasource.impl.APP_THEME
import dev.atajan.lingva_android.common.data.datasource.impl.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun selectedThemeFlow(applicationContext: Context): Flow<String?> {
    return applicationContext.dataStore.data
        .map { preferences ->
            preferences[APP_THEME]
        }
}