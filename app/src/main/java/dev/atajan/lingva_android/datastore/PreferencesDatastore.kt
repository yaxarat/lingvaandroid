package dev.atajan.lingva_android.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Name of the datastore.
 */
private const val PREFERENCE_NAME: String = "settings"

/**
 * Corresponding key type function to define a key for each value that you need to store.
 */
val IS_DARK_THEME: Preferences.Key<Boolean> = booleanPreferencesKey("is_dark_theme")

/**
 * Use the property delegate to create an instance of Datastore<Preferences>.
 * Call it once at the top level of your kotlin file, and access it through this property throughout the rest of your application.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)