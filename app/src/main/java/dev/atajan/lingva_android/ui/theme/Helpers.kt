package dev.atajan.lingva_android.ui.theme

import android.content.Context
import dev.atajan.lingva_android.datastore.APP_THEME
import dev.atajan.lingva_android.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun selectedThemeFlow(applicationContext: Context): Flow<String?> {
    return applicationContext.dataStore.data
        .map { preferences ->
            preferences[APP_THEME]
        }
}