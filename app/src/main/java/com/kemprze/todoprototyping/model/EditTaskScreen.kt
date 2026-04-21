package com.kemprze.todoprototyping.model

import android.R.attr.onClick
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kemprze.todoprototyping.data.model.Category
import com.kemprze.todoprototyping.data.model.Duration
import com.kemprze.todoprototyping.data.model.Priority
import com.kemprze.todoprototyping.data.model.ReminderOffset
import com.kemprze.todoprototyping.model.tasks.TasksViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: String,
    tasksViewModel: TasksViewModel,
    onNavigateBack: () -> Unit
) {
    val task = remember { tasksViewModel.getTaskById(taskId) } ?: return

    var taskName by remember { mutableStateOf(task.taskName) }
    var taskDescription by remember { mutableStateOf(task.taskDescription) }
    var priority by remember { mutableStateOf(task.priority) }
    var category by remember { mutableStateOf(task.category) }
    var duration by remember { mutableStateOf(task.duration) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var reminderOffset by remember { mutableStateOf(task.reminderOffset) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            tasksViewModel.onTaskUpdated(
                                task.copy(
                                    taskName = taskName,
                                    taskDescription = taskDescription,
                                    priority = priority,
                                    category = category,
                                    duration = duration,
                                    dueDate = dueDate,
                                    reminderOffset = reminderOffset
                                )
                            )
                            onNavigateBack()
                        }) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it},
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it},
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.entries.filter { it != Category.NONE }.forEach {
                    cat -> FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = {
                            Text("${stringResource(cat.categoryImageRes)}" +
                                    " ${stringResource(cat.categoryNameRes)}")
                        }
                    )
                }
            }
            Text(
                text = "Priority",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.entries.forEach {
                    pri ->
                    FilterChip(
                        selected = priority == pri,
                        onClick = { priority = pri },
                        label = { Text("${pri.emoji} ${pri.label}")}
                    )
                }
            }
            Text(
                text = "Duration",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Duration.entries.forEach {
                    dur ->
                    FilterChip(
                        selected = duration == dur,
                        onClick = { duration = dur},
                        label = { Text(dur.label) }
                    )
                }
            }
            Text(
                text = "Due date",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = dueDate?.toInstant(java.time.ZoneOffset.UTC)?.toEpochMilli()
            )
            var showDatePicker by remember { mutableStateOf(false) }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                millis ->
                                dueDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) { DatePicker(state = datePickerState )}
            }
            OutlinedButton(
                onClick = { showDatePicker = true},
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(dueDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "No date set")
            }
            Text(
                text = "Reminder",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderOffset.entries.forEach {
                    offset ->
                    FilterChip(
                        selected = reminderOffset == offset,
                        onClick = { reminderOffset = if (reminderOffset == offset) null else offset },
                        label = { Text(offset.label) }
                    )
                }
            }
        }
    }
}
