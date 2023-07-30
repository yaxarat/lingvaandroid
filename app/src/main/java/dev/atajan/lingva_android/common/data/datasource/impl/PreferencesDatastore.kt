package dev.atajan.lingva_android.common.data.datasource.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Name of the datastore.
 */
private const val PREFERENCE_NAME: String = "settings"

/**
 * Corresponding key type function to define a key for each value that you need to store.
 */
val APP_THEME: Preferences.Key<String> = stringPreferencesKey("app_theme")
val DEFAULT_SOURCE_LANGUAGE: Preferences.Key<String> = stringPreferencesKey("default_source_language")
val DEFAULT_TARGET_LANGUAGE: Preferences.Key<String> = stringPreferencesKey("default_target_language")
val CUSTOM_LINGVA_ENDPOINT: Preferences.Key<String> = stringPreferencesKey("custom_lingva_endpoint")
val LIVE_TRANSLATE_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("live_translate_enabled")

/**
 * Use the property delegate to create an instance of Datastore<Preferences>.
 * Call it once at the top level of your kotlin file, and access it through this property throughout the rest of your application.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)