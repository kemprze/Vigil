package com.kemprze.vigil.model.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kemprze.vigil.data.DarkModePreferences
import com.kemprze.vigil.data.SettingsDataStore
import com.kemprze.vigil.ui.theme.AppFont
import com.kemprze.vigil.ui.theme.AppTheme
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    val themeFlow = settingsDataStore.themeFlow
    val fontFlow = settingsDataStore.fontFlow
    val darkModeFlow = settingsDataStore.darkModeFlow
    val dynamicColorFlow = settingsDataStore.dynamicColorFlow
    val googleSyncFlow = settingsDataStore.googleSyncFlow
    val aiOptInFlow = settingsDataStore.aiOptInFlow
    val aiModelReadyFlow = settingsDataStore.aiModelReadyFlow

    fun saveTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsDataStore.saveTheme(theme)
        }
    }
    fun saveFont(font: AppFont) {
        viewModelScope.launch {
            settingsDataStore.saveFont(font)
        }
    }

    fun saveDarkMode(darkMode: DarkModePreferences) {
        viewModelScope.launch {
            settingsDataStore.saveDarkMode(darkMode)
        }
    }

    fun saveDynamicColor(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveDynamicColor(isEnabled)
        }
    }

    fun saveGoogleCalendarId(id: String) {
        viewModelScope.launch {
            settingsDataStore.saveGoogleSyncFlow(id)
        }
    }

    fun clearGoogleCalendarId() {
        viewModelScope.launch {
            settingsDataStore.clearGoogleSync()
        }
    }

    fun saveAiOptIn(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveAiOptIn(isEnabled)
        }
    }

    fun saveAiModelReady(isReady: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveAiModelReady(isReady)
        }
    }

}