package com.titoodev.h_gym.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.titoodev.h_gym.ui.theme.ThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_settings")

data class ThemeSettings(
    val variant: ThemeVariant,
    val isDarkMode: Boolean
)

class ThemePreferences(private val context: Context) {
    companion object {
        private val THEME_VARIANT_KEY = stringPreferencesKey("theme_variant")
        private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    }

    val themeSettingsFlow: Flow<ThemeSettings> = context.dataStore.data.map { prefs ->
        val variantName = prefs[THEME_VARIANT_KEY] ?: ThemeVariant.PINK.name
        val variant = try {
            ThemeVariant.valueOf(variantName)
        } catch (_: Exception) {
            ThemeVariant.PINK
        }
        val isDarkMode = prefs[IS_DARK_MODE_KEY] ?: true
        ThemeSettings(variant, isDarkMode)
    }

    suspend fun setThemeVariant(variant: ThemeVariant) {
        context.dataStore.edit { prefs ->
            prefs[THEME_VARIANT_KEY] = variant.name
        }
    }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE_KEY] = isDarkMode
        }
    }
}
