package com.kemprze.vigil.model.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kemprze.vigil.ai.DownloadModelWorker
import com.kemprze.vigil.data.DarkModePreferences
import com.kemprze.vigil.data.SettingsDataStore
import com.kemprze.vigil.ui.theme.AppFont
import com.kemprze.vigil.ui.theme.AppTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    val aiModelVariantFlow = settingsDataStore.aiModelVariantFlow
    val downloadProgressFlow = WorkManager.getInstance(getApplication())
        .getWorkInfosByTagFlow("download_model")
        .map {
            workInfos ->
            val info = workInfos.firstOrNull()

            when {
                info == null -> -1
                info.state.isFinished -> -1
                else ->
                    info.progress.getInt(DownloadModelWorker.KEY_PROGRESS, 0)
            }
        }
    val preferredNameFlow = settingsDataStore.preferredNameFlow
    val hasOnboardedFlow = settingsDataStore.hasOnboardedFlow

    val isDownloadWaitingFlow = WorkManager.getInstance(getApplication())
        .getWorkInfosByTagFlow("download_model")
        .map {
            workInfos ->
            workInfos.firstOrNull()?.state == WorkInfo.State.ENQUEUED
        }

    init {
        viewModelScope.launch {
            aiOptInFlow.collect {
                optedIn ->
                if (optedIn == true) {
                    enqueueModelDownload()
                }
            }
        }
    }
    fun downloadModelOnMobileData() {
        WorkManager.getInstance(getApplication()).cancelUniqueWork("download_model")
        enqueueModelDownload(wifiOnly = false)
    }

    private fun enqueueModelDownload(wifiOnly: Boolean = true) {
        viewModelScope.launch {
            val variant = settingsDataStore.aiModelVariantFlow.first()
            val file = DownloadModelWorker.modelFile(getApplication(), variant)

            if (file.exists()) {
                settingsDataStore.saveAiModelReady(true)
            } else {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<DownloadModelWorker>()
                    .setInputData(workDataOf(DownloadModelWorker.KEY_VARIANT to variant))
                    .apply {
                        if (wifiOnly) {
                            setConstraints(constraints)
                        }
                    }
                    .addTag("download_model")
                    .build()

                WorkManager.getInstance(getApplication())
                    .enqueueUniqueWork(
                        "download_model",
                        ExistingWorkPolicy.KEEP,
                        workRequest
                    )
            }
        }
    }

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

    fun saveSelectedModelVariant(variant: String) {
        viewModelScope.launch {
            settingsDataStore.saveAiModelVariant(variant)
        }
    }

    fun savePreferredName(name: String) {
        viewModelScope.launch {
            settingsDataStore.savePreferredName(name)
        }
    }

    fun saveFeedbackStyle(style: String) {
        viewModelScope.launch {
            settingsDataStore.saveFeedbackStyle(style)
        }
    }

    fun saveOnboardingCompleted(hasCompleted: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveOnboardingCompleted(hasCompleted)
        }
    }

    fun clearAiModel() {
        viewModelScope.launch {
            val currentModel = settingsDataStore.aiModelVariantFlow.first()
            val file = DownloadModelWorker.modelFile(getApplication(), currentModel)
            file.delete()
            settingsDataStore.clearAiModel()
            WorkManager.getInstance(getApplication()).cancelUniqueWork("download_model")
        }
    }

}