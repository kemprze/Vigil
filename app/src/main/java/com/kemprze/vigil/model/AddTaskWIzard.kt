package com.kemprze.vigil.model

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Duration
import com.kemprze.vigil.data.model.Priority
import com.kemprze.vigil.data.model.ReminderOffset
import com.kemprze.vigil.data.model.SimpleTask
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskWizard(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onAddClick: (SimpleTask) -> Unit
) {
    val pageCount = 5
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    var taskName by rememberSaveable { mutableStateOf("") }
    var taskDescription by rememberSaveable { mutableStateOf("")}
    var priority by rememberSaveable { mutableStateOf(Priority.NORMAL) }
    var category by rememberSaveable { mutableStateOf(Category.NONE) }
    var dueDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var dueTimeHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var dueTimeMinute by rememberSaveable { mutableStateOf<Int?>(null) }
    var needsReminder by rememberSaveable { mutableStateOf(false) }
    var duration by rememberSaveable { mutableIntStateOf(0) }
    var reminderOffset by rememberSaveable {
        mutableStateOf<ReminderOffset?>(null) }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var showNameError by remember { mutableStateOf(false) }

    Scaffold(modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1) / pageCount.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) {
                page ->
                when (page) {
                    0 -> WizardStepName(
                        taskName = taskName,
                        onTaskNameChange = { taskName = it },
                        isError = taskName.isBlank(),
                        showNameError = showNameError,
                        onErrorCleared = { showNameError = false }
                    )
                    1 -> WizardStepDetails(
                        taskDescription = taskDescription,
                        onTaskDescriptionChange = { taskDescription = it},
                        selectedCategory = category,
                        onCategorySelected = { category = it }
                    )
                    2 -> WizardStepWhen(
                        dueDateMillis = dueDate,
                        onDateSelected = { dueDate = it },
                        needsReminder = needsReminder,
                        onReminderChange = { needsReminder = it },
                        selectedOffset = reminderOffset,
                        onOffsetSelected = { reminderOffset = it },
                        dueTimeHour = dueTimeHour,
                        dueTimeMinute = dueTimeMinute,
                        onTimeSet = { hour, minute ->
                            dueTimeHour = hour
                            dueTimeMinute = minute
                        },
                        onTimeCleared = {
                            dueTimeHour = null
                            dueTimeMinute = null
                        }
                    )
                    3 -> WizardStepDuration(
                        duration = duration,
                        onDurationChange = { duration = it }
                    )
                    4 -> WizardStepImportance(
                        priority = priority,
                        onPriorityChange = { priority = it }
                    )
                }
            }
            Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else {
                            onNavigateBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Skip button — hidden on last page
                if (pagerState.currentPage > 0 && pagerState.currentPage < pageCount - 1) {
                    TextButton(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }) {
                        Text("Skip")
                    }
                }

                // Next / Confirm button
                Button(onClick = {
                    if (pagerState.currentPage == 0 && taskName.isBlank()) {
                        showNameError = true
                        return@Button
                    }
                     else if (pagerState.currentPage < pageCount - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        if (!isSubmitting) {
                            val newTask = SimpleTask(
                                taskName = taskName,
                                taskDescription = taskDescription,
                                priority = priority,
                                category = category,
                                dueDate = dueDate?.let { millis ->
                                    val date = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    if (dueTimeHour != null && dueTimeMinute != null)
                                        date.atTime(dueTimeHour!!, dueTimeMinute!!)
                                    else
                                        date.atStartOfDay()
                                },
                                needsReminder = needsReminder,
                                remindMe = dueDate?.let { millis ->
                                    val due = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime()
                                    reminderOffset?.calculateReminderTime(due)
                                },
                                duration = Duration.fromMinutes(duration)
                            )
                            onAddClick(newTask)
                        }
                    }
                },
                    enabled = !isSubmitting
                ) {
                    Text(if (pagerState.currentPage < pageCount - 1) "Next" else "Done")
                }
            }
        }
    }
}

@Composable
fun WizardStepName(
    taskName: String,
    isError: Boolean,
    showNameError: Boolean,
    onErrorCleared: () -> Unit,
    onTaskNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = "What do you need to do?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(32.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            OutlinedTextField(
                value = taskName,
                onValueChange = {
                    onTaskNameChange(it)
                    onErrorCleared()
                                },
                isError = isError,
                supportingText = { if (showNameError) Text("Task name cannot be left empty") },
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun WizardStepDetails(
    taskDescription: String,
    onTaskDescriptionChange: (String) -> Unit,
    selectedCategory: Category,
    onCategorySelected: (Category) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = "Any details?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Category.entries) {
                        cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onCategorySelected(cat) },
                            label = {
                                Text(
                                    if (cat == Category.NONE) "🚫 None"
                                    else "${stringResource(id = cat.categoryImageRes)} ${stringResource(id = cat.categoryNameRes)}"
                                )
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.TopCenter
        ) {
            OutlinedTextField(
                value = taskDescription,
                onValueChange = onTaskDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardStepWhen(
    dueDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    needsReminder: Boolean,
    onReminderChange: (Boolean) -> Unit,
    selectedOffset: ReminderOffset?,
    onOffsetSelected: (ReminderOffset) -> Unit,
    dueTimeHour: Int?,
    dueTimeMinute: Int?,
    onTimeSet: (Int, Int) -> Unit,
    onTimeCleared: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {

            }
        })
    var hasTime by remember { mutableStateOf(dueTimeHour != null) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = dueTimeHour ?: 12,
        initialMinute = dueTimeMinute ?: 0
    )


    LaunchedEffect(
        datePickerState.selectedDateMillis
    ) {
        onDateSelected(datePickerState.selectedDateMillis)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "When do you need\nto do this?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1.6f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 12.dp)
        ) {
            DatePicker(
                modifier = Modifier.padding(top = 24.dp),
                state = datePickerState,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    headlineContentColor = MaterialTheme.colorScheme.onPrimary,
                    weekdayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    dayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContentColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.onPrimary,
                    navigationContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                showModeToggle = false
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Set time",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Switch(
                    checked = hasTime,
                    onCheckedChange = {
                            isChecked ->
                        hasTime = isChecked
                        if (isChecked) showTimePicker = true
                        else onTimeCleared()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary
                    )

                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Remind me",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Switch(
                    checked = needsReminder,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        onReminderChange(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            if (needsReminder) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(ReminderOffset.entries) {
                            offset ->
                        FilterChip(
                            selected = selectedOffset == offset,
                            onClick = { onOffsetSelected(offset) },
                            label = { Text(offset.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedOffset == offset,
                                borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            if (showTimePicker) {
                ModalBottomSheet(
                    onDismissRequest = {
                        if (dueTimeHour == null) hasTime = false
                        showTimePicker = false
                    },
                    sheetState = sheetState
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Set time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TimePicker(state = timePickerState)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    hasTime = false
                                    onTimeCleared()
                                    showTimePicker = false
                                }
                            ) {
                                Text(
                                    text = "Cancel"
                                    // potential color issue, needs checking
                                )
                            }
                            TextButton(onClick = {
                                onTimeSet(timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            }) {
                                Text(
                                    text = "Confirm",
                                    // potential color issue, needs checking
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStepDuration(
    duration: Int,
    onDurationChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = "How long will\nthis take?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(36.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = Duration.fromMinutes(duration).label)
                Slider(
                    value = duration.toFloat(),
                    onValueChange = { onDurationChange(it.toInt()) },
                    valueRange = 0f..60f
                )
            }
        }
    }
}

@Composable
fun WizardStepImportance(
    priority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = "How much does\nthis matter?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(36.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Priority.entries.forEach {
                    p -> FilterChip(
                        selected = priority == p,
                        onClick = { onPriorityChange(p) },
                        label = {
                            Text("${p.emoji} ${p.label}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = priority == p,
                            borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            selectedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    )
                }
            }
        }
    }
}