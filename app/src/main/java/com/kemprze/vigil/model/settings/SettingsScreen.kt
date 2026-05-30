package com.kemprze.vigil.model.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.kemprze.vigil.R
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
        try {
            task.getResult(ApiException::class.java)
            isConnected = true
            scope.launch {
                val calendarId = GoogleCalendarSync.setupVigilCalendar(context)
                if (calendarId != null) {
                    settingsViewModel.saveGoogleCalendarId(calendarId)
                }
            }
        } catch (e: ApiException) {
            isConnected = false
        }
    }

    val aiOptIn by settingsViewModel.aiOptInFlow.collectAsState(
        initial = false
    )

    val aiModelReady by settingsViewModel.aiModelReadyFlow.collectAsState(
        initial = false
    )
    var showAiPrivacyDialog by remember { mutableStateOf(false) }
    var selectedModelVariant by remember { mutableStateOf("E2B") }
    val aiModelVariant by settingsViewModel.aiModelVariantFlow.collectAsState(initial = "E2B")
    val downloadProgress by settingsViewModel.downloadProgressFlow.collectAsState(initial = -1)
    val isDownloadWaiting by settingsViewModel.isDownloadWaitingFlow.collectAsState(initial = false)
    val selectedFeedbackStyle by settingsViewModel.feedbackStyleFlow.collectAsState(initial = "encouraging")

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.dialog_title_before_connect)) },
            text = {
                Text(stringResource(R.string.dialog_text_google_privacy))
            },
            confirmButton = { TextButton(
                onClick = {
                    showPrivacyDialog = false
                    val signInIntent = GoogleCalendarSync
                        .getGoogleSignInClient(context)
                        .signInIntent
                    signInLauncher.launch(signInIntent)
                }
            ) { Text(stringResource(R.string.btn_i_understand_connect)) }
            },
            dismissButton = {
                TextButton( onClick = { showPrivacyDialog = false} ) { Text(stringResource(R.string.btn_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.title_color_mode),
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
                        text = stringResource(R.string.title_dynamic_color),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { settingsViewModel.saveDynamicColor(it) },
                    )
                }

                if (!dynamicColor) {
                    Text(
                        text = stringResource(R.string.title_theme),
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
                    text = stringResource(R.string.title_font),
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
                    text = stringResource(R.string.title_sync),
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_google_calendar),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (isConnected) stringResource(R.string.label_connected) else stringResource(R.string.label_not_connected),
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
                        content = { Text( if (isConnected) stringResource(R.string.btn_disconnect) else stringResource(R.string.btn_connect)) })
                }

                HorizontalDivider()
                Text(
                    text = stringResource(R.string.title_ai_features),
                    style = MaterialTheme.typography.titleMedium
                )

                if (showAiPrivacyDialog) {
                    AlertDialog(
                        onDismissRequest = { showAiPrivacyDialog = false },
                        title = { Text(stringResource(R.string.dialog_title_before_ai)) },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                            Text(stringResource(R.string.dialog_text_ai_privacy))
                            HorizontalDivider()
                                Text(
                                    text = stringResource(R.string.label_choose_model),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                listOf(
                                    Triple("E2B", stringResource(R.string.model_e2b_label), stringResource(R.string.model_e2b_description)),
                                    Triple("E4B", stringResource(R.string.model_e4b_label), stringResource(R.string.model_e4b_description))
                                ).forEach { (variant, label, description) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedModelVariant = variant
                                            }
                                            .padding(
                                                vertical = 2.dp
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedModelVariant == variant,
                                            onClick = { selectedModelVariant = variant }
                                        )
                                        Column {
                                            Text(label, style = MaterialTheme.typography.bodyLarge)
                                            Text(
                                                description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }

                               },
                        confirmButton = { TextButton(
                            onClick = {
                                showAiPrivacyDialog = false
                                settingsViewModel.saveAiOptIn(true)
                                settingsViewModel.saveSelectedModelVariant(selectedModelVariant)
                            }
                        ) {
                            Text(stringResource(R.string.btn_i_understand_enable))
                        }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showAiPrivacyDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_on_device_ai),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = when {
                                aiModelReady!! -> "Ready · $aiModelVariant"
                                    aiOptIn!! -> stringResource(R.string.label_downloading_model)
                                    else -> stringResource(R.string.label_not_enabled)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                aiModelReady -> MaterialTheme.colorScheme.primary
                                    aiOptIn -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                    Button(
                        onClick = {
                            if (aiOptIn) {
                                settingsViewModel.saveAiOptIn(false)
                                settingsViewModel.saveAiModelReady(false)
                                settingsViewModel.clearAiModel()
                                } else {
                                    showAiPrivacyDialog = true
                            }
                        }
                    ) {
                        Text(if (aiOptIn!!) stringResource(R.string.btn_disable) else stringResource(R.string.btn_enable))
                    }


                }
                if (downloadProgress >= 0) {
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (aiOptIn && !aiModelReady && isDownloadWaiting) {
                    Text(
                        text = stringResource(R.string.msg_wifi_waiting),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(onClick = { settingsViewModel.downloadModelOnMobileData() } ) {
                        Text(
                            text = stringResource(R.string.btn_download_anyway),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                            )
                    }
                }

                if (aiOptIn && aiModelReady) {
                    Text(
                        text = stringResource(R.string.label_select_feedback_style),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(FeedbackStyle.entries) { feedbackStyle ->
                            FilterChip(
                                selected = feedbackStyle.name.lowercase() == selectedFeedbackStyle,
                                onClick = { settingsViewModel.saveFeedbackStyle( feedbackStyle.name.lowercase()) },
                                label = { Text(
                                    text = "${stringResource(feedbackStyle.emojiRes)} ${stringResource(feedbackStyle.labelRes)}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )}
                            )
                        }
                    }
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
                    text = stringResource(R.string.font_preview_sample),
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
internal fun ThemeChip(
    theme: AppTheme,
    isDark: Boolean,
    selected: Boolean,
    size: Dp = 48.dp,
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
            .width(size)
            .height(size)
    )
}