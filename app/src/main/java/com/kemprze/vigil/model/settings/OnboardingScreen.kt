package com.kemprze.vigil.model.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kemprze.vigil.R
import com.kemprze.vigil.data.DarkModePreferences
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.model.tasks.TasksViewModel
import com.kemprze.vigil.ui.theme.AppTheme
import com.kemprze.vigil.ui.theme.themePrimaryColor


data class StarterTask(val name: String, val category: Category)

val starterTasks = listOf(
    StarterTask("Do the dishes", Category.HOME),
    StarterTask("Run a load of laundry", Category.HOME),
    StarterTask("Vacuum / sweep the floors", Category.HOME),
    StarterTask("Change the bed sheets", Category.HOME),
    StarterTask("Restock groceries", Category.SHOPPING),
    StarterTask("Prep a meal", Category.HOME),
    StarterTask("Take out the trash", Category.HOME),
    StarterTask("Take medication / vitamins", Category.HEALTH),
    StarterTask("Drink a glass of water", Category.HEALTH),
    StarterTask("Do a skincare routine", Category.HEALTH),
    StarterTask("Charge devices for tomorrow", Category.PERSONAL),
    StarterTask("Check bank balance", Category.FINANCE),
    StarterTask("Sort through mail", Category.PERSONAL),
    StarterTask("Clear unread emails", Category.PERSONAL),
    StarterTask("Water the plants", Category.HOME)
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizard(
    settingsViewModel: SettingsViewModel,
    tasksViewModel: TasksViewModel
) {
    val pageCount = 5
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
    var userName by rememberSaveable { mutableStateOf("") }
    val selectedStarterTasks = remember { mutableStateListOf<StarterTask>() }
    var feedbackStyle by rememberSaveable { mutableStateOf(FeedbackStyle.ENCOURAGING) }
    val currentColorMode by settingsViewModel.darkModeFlow.collectAsState(initial = DarkModePreferences.SYSTEM)
    val dynamicColor by settingsViewModel.dynamicColorFlow.collectAsState(initial = false)
    val appTheme by settingsViewModel.themeFlow.collectAsState(initial = AppTheme.SCARLET)
    val aiOptIn by settingsViewModel.aiOptInFlow.collectAsState(initial = false)
    var notificationsEnabled by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1) / pageCount.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WizardStepWelcome()
                    1 -> WizardStepUserName(
                        userName = userName,
                        onValueChange = { userName = it }
                    )

                    2 -> WizardStepStarterTasks(
                        selectedStarterTasks = selectedStarterTasks,
                        onChipClick = {
                            if (selectedStarterTasks.contains(it)) selectedStarterTasks.remove(it) else selectedStarterTasks.add(
                                it
                            )
                        }
                    )

                    3 -> WizardStepQuickSettings(
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsChange = { notificationsEnabled = it },
                        isAiOptIn = aiOptIn,
                        onAiOptInChange = { settingsViewModel.saveAiOptIn(it) },
                        selectedFeedbackStyle = feedbackStyle,
                        onFeedbackStyleChange = { feedbackStyle = it },
                        isDynamicColorOn = dynamicColor,
                        onDynamicColorChange = { settingsViewModel.saveDynamicColor(it) },
                        appTheme = appTheme,
                        onThemeSelected = { settingsViewModel.saveTheme(it) },
                        currentColorMode = currentColorMode,
                        onCurrentColorModeChange = { settingsViewModel.saveDarkMode(it) }
                    )
                    4 -> WizardStepGetStarted(
                        onOnboardingComplete = {
                            settingsViewModel.savePreferredName(userName)
                            settingsViewModel.saveTheme(appTheme)
                            settingsViewModel.saveDynamicColor(dynamicColor)
                            settingsViewModel.saveAiOptIn(aiOptIn)
                            settingsViewModel.saveFeedbackStyle(feedbackStyle.name.lowercase())
                            selectedStarterTasks.forEach { task ->
                                tasksViewModel.onTaskAdded(
                                    taskName = task.name,
                                    category = task.category
                                )
                            }
                            settingsViewModel.saveOnboardingCompleted(true)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun WizardStepWelcome(
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = stringResource(R.string.onboarding_hello),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_lets_get_started),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun WizardStepUserName(
    userName: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Text(
            text = stringResource(R.string.onboarding_what_to_call_you),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center
            )
        )

    }
}

@Composable
fun WizardStepStarterTasks(
    selectedStarterTasks: SnapshotStateList<StarterTask>,
    onChipClick: (StarterTask) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_starter_tasks_prompt),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            starterTasks.groupBy { it.category }.forEach {
                (category, tasks) ->
                Text(
                    text = "${stringResource(category.categoryImageRes)}  ${stringResource(category.categoryNameRes)}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(
                    modifier = Modifier.height(6.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = 24.dp
                    )
                )
                Spacer(
                    modifier = Modifier.height(2.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    tasks.forEach { task ->
                        FilterChip(
                            selected = selectedStarterTasks.contains(task),
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = MaterialTheme.colorScheme.background,
                                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                                selectedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7F),
                                selectedLabelColor = MaterialTheme.colorScheme.background
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedStarterTasks.contains(task),
                                borderColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                selectedBorderColor = MaterialTheme.colorScheme.background
                            ),
                            onClick = { onChipClick(task) },
                            modifier = Modifier.padding(2.dp),
                            label = {
                                Text(
                                    text = task.name,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStepQuickSettings(
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    isAiOptIn: Boolean,
    onAiOptInChange: (Boolean) -> Unit,
    selectedFeedbackStyle: FeedbackStyle,
    onFeedbackStyleChange: (FeedbackStyle) -> Unit,
    isDynamicColorOn: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    currentColorMode: DarkModePreferences,
    onCurrentColorModeChange: (DarkModePreferences) -> Unit,
    appTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
                onNotificationsChange(isGranted)
        }
    )



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.onboarding_notifications),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(R.string.onboarding_turn_on_notifications),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onNotificationsChange(isChecked)
                    }
                }
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_look_and_feel),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.onboarding_use_dynamic_color),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = isDynamicColorOn,
                onCheckedChange = { onDynamicColorChange(it) }
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.onboarding_color_mode_preferences),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow {
                DarkModePreferences.entries.forEachIndexed {
                        index, mode ->
                    SegmentedButton(
                        selected = currentColorMode == mode,
                        onClick = { onCurrentColorModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, DarkModePreferences.entries.size),
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase() }) }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (!isDynamicColorOn) {
            Text(
                text = stringResource(R.string.onboarding_select_theme),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    ThemeColorCircle(
                        theme = theme,
                        selected = theme == appTheme,
                        onClick = { onThemeSelected(theme) },
                        isDark = when (currentColorMode) {
                            DarkModePreferences.DARK -> true
                            DarkModePreferences.LIGHT -> false
                            DarkModePreferences.SYSTEM -> isSystemInDarkTheme()
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = stringResource(R.string.onboarding_ai_usage),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.onboarding_wanna_use_ai),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = isAiOptIn,
                onCheckedChange = { onAiOptInChange(it) },
            )
        }

        Spacer(Modifier.height(12.dp))

        if (isAiOptIn) {
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
                        selected = feedbackStyle == selectedFeedbackStyle ,
                        onClick = { onFeedbackStyleChange(feedbackStyle) },
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

@Composable
fun WizardStepGetStarted(
    onOnboardingComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            modifier = Modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            onClick = onOnboardingComplete,
        ) {
            Text(
                text = stringResource(R.string.onboarding_get_started_btn),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun ThemeColorCircle(
    theme: AppTheme,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    size: Dp = 36.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(themePrimaryColor(theme, isDark))
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable { onClick() },
    )
}
