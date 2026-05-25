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
        val AI_OPT_IN = booleanPreferencesKey("ai_opt_in")
        val AI_MODEL_VARIANT = stringPreferencesKey("ai_model_variant")
        val AI_MODEL_READY = booleanPreferencesKey("ai_model_ready")
        val PREFERRED_NAME = stringPreferencesKey("preferred_name")
        val FEEDBACK_STYLE = stringPreferencesKey("feedback_style")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("has_completed_onboarding")
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

    val aiOptInFlow: Flow<Boolean> = context.dataStore.data.map {
        prefs -> prefs[AI_OPT_IN]  ?: false
    }

    val aiModelVariantFlow: Flow<String> = context.dataStore.data.map {
        prefs -> prefs[AI_MODEL_VARIANT] ?: "E2B"
    }

    val aiModelReadyFlow: Flow<Boolean> = context.dataStore.data.map {
        prefs -> prefs[AI_MODEL_READY] ?: false
    }

    val hasOnboardedFlow: Flow<Boolean> = context.dataStore.data.map {
        prefs -> prefs[ONBOARDING_COMPLETED] ?: false
    }

    val preferredNameFlow: Flow<String> = context.dataStore.data.map {
        prefs -> prefs[PREFERRED_NAME] ?: "friend"
    }

    val feedbackStyleFlow: Flow<String> = context.dataStore.data.map {
        prefs -> prefs[FEEDBACK_STYLE] ?: "encouraging"
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

    suspend fun savePreferredName(name: String) {
        context.dataStore.edit {
            prefs -> prefs[PREFERRED_NAME] = name
        }
    }

    suspend fun saveFeedbackStyle(style: String) {
        context.dataStore.edit {
            prefs -> prefs[FEEDBACK_STYLE] = style
        }
    }

    suspend fun saveOnboardingCompleted(hasCompleted: Boolean) {
        context.dataStore.edit {
            prefs -> prefs[ONBOARDING_COMPLETED] = hasCompleted
        }
    }

    suspend fun clearGoogleSync() {
        context.dataStore.edit {
            prefs ->
            prefs.remove(GOOGLE_CALENDAR_SYNC_ID)
        }
    }

    suspend fun saveAiOptIn(isEnabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[AI_OPT_IN] = isEnabled}
    }

    suspend fun saveAiModelReady(isReady: Boolean) {
        context.dataStore.edit {
            prefs -> prefs[AI_MODEL_READY] = isReady
        }
    }

    suspend fun clearAiModel() {
        saveAiModelReady(false)
        saveAiOptIn(false)

        // to add file deletion once the model path is known
    }

    suspend fun saveAiModelVariant(aiModelVariant: String) {
        context.dataStore.edit {
            prefs -> prefs[AI_MODEL_VARIANT] = aiModelVariant
        }
    }
}