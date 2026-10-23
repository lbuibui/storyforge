package com.storyforge.nativerewrite.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "storyforge_native_settings")

data class SettingsSnapshot(
    val themeMode: String = "system",
    val dynamicColor: Boolean = true,
)

class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")

    val snapshot: Flow<SettingsSnapshot> = context.settingsStore.data.map { prefs ->
        SettingsSnapshot(
            themeMode = prefs[themeKey] ?: "system",
            dynamicColor = prefs[dynamicKey] ?: true,
        )
    }

    suspend fun setThemeMode(mode: String) {
        require(mode in setOf("system", "light", "dark"))
        context.settingsStore.edit { it[themeKey] = mode }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsStore.edit { it[dynamicKey] = enabled }
    }
}
