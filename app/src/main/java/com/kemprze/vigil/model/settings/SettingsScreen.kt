package com.kemprze.vigil.model.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.kemprze.vigil.data.DarkModePreferences
import com.kemprze.vigil.sync.GoogleCalendarSync
import com.kemprze.vigil.ui.theme.AppFont
import com.kemprze.vigil.ui.theme.AppTheme
import com.kemprze.vigil.ui.theme.fontFamilyFor
import com.kemprze.vigil.ui.theme.themePrimaryColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val appTheme by settingsViewModel.themeFlow.collectAsState(initial = AppTheme.SCARLET)
    val appFont by settingsViewModel.fontFlow.collectAsState(initial = AppFont.LATO)
    val darkMode by settingsViewModel.darkModeFlow.collectAsState(initial = DarkModePreferences.SYSTEM)
    val dynamicColor by settingsViewModel.dynamicColorFlow.collectAsState(initial = false)
    val googleCalendarId by settingsViewModel.googleSyncFlow.collectAsState(initial = null)

    val context = LocalContext.current
    var isConnected = googleCalendarId != null
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        android.util.Log.d("VIGILSync", "Launcher result received, result code: ${result.resultCode}")
        try {
            task.getResult(ApiException::class.java)
            isConnected = true
            scope.launch {
                val calendarId = GoogleCalendarSync.setupVigilCalendar(context)
                android.util.Log.d("VIGILSync", "Calendar ID returned: $calendarId")
                if (calendarId != null) {
                    settingsViewModel.saveGoogleCalendarId(calendarId)
                    android.util.Log.d("VIGILSync", "Calendar ID saved")
                }
            }
        } catch (e: ApiException) {
            android.util.Log.d("VIGILSync", "Sign-in failed, error code: ${e.statusCode}")
            isConnected = false
        }
    }


    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Before you connect") },
            text = {
                Text("Connecting Google Calendar will send your task names, dates and times to Google Servers. This data will be subject to Google's privacy policy. Your tasks are currently stored only on your device.")
            },
            confirmButton = { TextButton(
                onClick = {
                    showPrivacyDialog = false
                    val signInIntent = GoogleCalendarSync
                        .getGoogleSignInClient(context)
                        .signInIntent
                    signInLauncher.launch(signInIntent)
                }
            ) { Text("I understand, connect") }
            },
            dismissButton = {
                TextButton( onClick = { showPrivacyDialog = false} ) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            ) },
        ) {
            innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Color mode",
                    style = MaterialTheme.typography.titleMedium
                )
                SingleChoiceSegmentedButtonRow() { 
                    DarkModePreferences.entries.forEachIndexed {
                        index, mode ->
                        SegmentedButton(
                            selected = darkMode == mode,
                            onClick = { settingsViewModel.saveDarkMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, DarkModePreferences.entries.size),
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase() }) }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dynamic color",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { settingsViewModel.saveDynamicColor(it) },
                    )
                }

                if (!dynamicColor) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium
                    )
                    val isDark = isSystemInDarkTheme()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            ThemeChip(
                                theme = theme,
                                selected = appTheme == theme,
                                isDark = isDark,
                                onClick = { settingsViewModel.saveTheme(theme) }
                            )
                        }
                    }
                }

                Text(
                    text = "Font",
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppFont.entries.forEach {
                        font ->
                        FontCard(
                            font = font,
                            selected = appFont == font,
                            onClick = { settingsViewModel.saveFont(font) }
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Google Calendar",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (isConnected) "Connected" else "Not connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Button(
                        onClick = {if (isConnected) {
                            GoogleCalendarSync
                                .getGoogleSignInClient(context)
                                .signOut()
                            settingsViewModel.clearGoogleCalendarId()
                            isConnected = false
                        } else {
                            showPrivacyDialog = true
                        }
                                  },
                        content = { Text( if (isConnected) "Disconnect" else "Connect") })
                }
            }
        }
    }

@Composable
private fun FontCard(
    font: AppFont,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = when (font) {
        AppFont.PLAYFAIR -> "Playfair"
        AppFont.LORA -> "Lora"
        AppFont.MONTSERRAT -> "Montserrat"
        AppFont.LATO -> "Lato"
        AppFont.ATKINSON -> "Atkinson"
        AppFont.COURIER_PRIME -> "Courier"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aa",
                    fontFamily = fontFamilyFor(font),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = label,
                    fontFamily = fontFamilyFor(font),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = Modifier
            .width(92.dp)
            .height(76.dp)
            .padding(top = 4.dp)
    )
}

@Composable
private fun ThemeChip(
    theme: AppTheme,
    isDark: Boolean,
    selected: Boolean,
    onClick: () -> Unit
    ) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = themePrimaryColor(theme, isDark),
            selectedContainerColor = themePrimaryColor(theme, isDark)
        ),
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
    )
}