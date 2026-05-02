package com.kemprze.vigil.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kemprze.vigil.ui.theme.AppFont
import com.kemprze.vigil.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val FONT_KEY = stringPreferencesKey("app_font")
        val IS_DARK_MODE = stringPreferencesKey("is_dark_mode")
        val IS_DYNAMIC_COLOR = booleanPreferencesKey("is_dynamic_color")
        val GOOGLE_CALENDAR_SYNC_ID = stringPreferencesKey("google_calendar_id")
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data.map {
        prefs -> val name = prefs[THEME_KEY] ?: AppTheme.SCARLET.name
        AppTheme.valueOf(name)
    }

    val fontFlow: Flow<AppFont> = context.dataStore.data.map {
        prefs -> val name = prefs[FONT_KEY] ?: AppFont.LATO.name
        AppFont.valueOf(name)
    }

    val darkModeFlow: Flow<DarkModePreferences> = context.dataStore.data.map {
        prefs -> val name = prefs[IS_DARK_MODE] ?: DarkModePreferences.SYSTEM.name
        DarkModePreferences.valueOf(name)
    }

    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data.map {
        prefs -> prefs[IS_DYNAMIC_COLOR] ?: false

    }

    val googleSyncFlow: Flow<String?> = context.dataStore.data.map {
        prefs -> prefs[GOOGLE_CALENDAR_SYNC_ID]
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { prefs -> prefs[THEME_KEY] = theme.name }
    }

    suspend fun saveFont(font: AppFont) {
        context.dataStore.edit { prefs -> prefs[FONT_KEY] = font.name}
    }

    suspend fun saveDarkMode(darkMode: DarkModePreferences) {
        context.dataStore.edit { prefs -> prefs[IS_DARK_MODE] = darkMode.name}
    }

    suspend fun saveDynamicColor(isEnabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_DYNAMIC_COLOR] = isEnabled}
    }

    suspend fun saveGoogleSyncFlow(googleCalendarId: String) {
        context.dataStore.edit {
            prefs -> prefs[GOOGLE_CALENDAR_SYNC_ID] = googleCalendarId
        }
    }

    suspend fun clearGoogleSync() {
        context.dataStore.edit {
            prefs ->
            prefs.remove(GOOGLE_CALENDAR_SYNC_ID)
        }
    }
}