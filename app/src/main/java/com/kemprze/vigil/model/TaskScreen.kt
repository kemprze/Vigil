package com.kemprze.vigil.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kemprze.vigil.R
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Duration
import com.kemprze.vigil.data.model.FilterState
import com.kemprze.vigil.data.model.Priority
import com.kemprze.vigil.data.model.SortOrder
import com.kemprze.vigil.data.model.Task
import com.kemprze.vigil.model.tasks.TasksViewModel
import com.kemprze.vigil.ui.theme.TODOPrototypingTheme
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    tasksViewModel: TasksViewModel = viewModel(),
    onNavigateToAddTask: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCalendarClick: () -> Unit,
    onStatsClick: () -> Unit,
    onEditClick: (Task) -> Unit
) {

    val taskUiState by tasksViewModel.uiState.collectAsState()
    var currentList by remember { mutableStateOf(ListTypes.INCOMPLETE) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val suggestedSubtasks by tasksViewModel.suggestedSubtasks.collectAsState()
    val isBreakingDown by tasksViewModel.isBreakingDown.collectAsState()
    val aiModelReady by tasksViewModel.aiModelReadyFlow.collectAsState(initial = false)
    val preferredName by tasksViewModel.preferredNameFlow.collectAsState(initial = "")

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showFilterSheet = false
            }
        ) {
            FilterSheetContent(
                filterState = taskUiState.filterState,
                onFilterChanged = { newFilter ->
                    tasksViewModel.onFilterChanged(newFilter)
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
    Scaffold(
        topBar = {
            MainTaskScreenAppBar(
                onListTypeChange = { newListType ->
                    currentList = newListType
                },
                currentList = currentList,
                preferredName = preferredName,
                onSettingsClick = onNavigateToSettings,
                onFilterClick = { showFilterSheet = true },
                filterActive = taskUiState.filterState.isActive
            )
        },
        bottomBar = {
            MainTaskScreenBottomAppBar(
                onAddClick = onNavigateToAddTask,
                onCalendarClick = onCalendarClick,
                onStatsClick = onStatsClick
            )
        }
    ) { innerPadding ->
        TaskList(
            currentListType = currentList,
            incompleteTasks = taskUiState.tasks,
            completeTasks = taskUiState.completedTasks,
            allTasks = taskUiState.allTasks,
            onTaskCompleted = { task, isCompleted ->
                tasksViewModel.onTaskCompleted(
                    task,
                    isCompleted
                )
            },
            onTaskDeleted = { task -> tasksViewModel.onTaskDeleted(task) },
            onEditClick = onEditClick,
            onSubtaskCompleted = { task, isCompleted ->
                tasksViewModel.onTaskCompleted(
                    task,
                    isCompleted
                )
            },
            onBreakdownClick = { task -> tasksViewModel.breakdownTask(task) },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            aiModelReady = aiModelReady
        )

        if (suggestedSubtasks.isNotEmpty()) {
            ModalBottomSheet(
                onDismissRequest = { tasksViewModel.clearSuggestedSubtasks() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Suggested subtasks",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    suggestedSubtasks.forEach { subtask ->
                        Text(
                            text = "• $subtask",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                tasksViewModel.clearSuggestedSubtasks()
                            }
                        ) {
                            Text("Dismiss")
                        }
                        TextButton(
                            onClick = {
                                val task = tasksViewModel.currentBreakdownTask.value

                                if (task != null) {
                                    suggestedSubtasks.forEach { subtaskName ->
                                        tasksViewModel.onSubtaskAdded(task, subtaskName)
                                    }
                                }
                                tasksViewModel.clearSuggestedSubtasks()
                            }
                        ) {
                            Text("Add all")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTaskScreenAppBar(
    modifier: Modifier = Modifier,
    currentList: ListTypes,
    filterActive: Boolean,
    preferredName: String,
    onListTypeChange: (ListTypes) -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.height(64.dp),
        windowInsets = WindowInsets(top = 0.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            IconButton(
                onClick = {
                    onListTypeChange(
                        if (currentList == ListTypes.INCOMPLETE) ListTypes.COMPLETE else ListTypes.INCOMPLETE
                    )
                }
            ) {
                Icon(
                    imageVector = if (currentList == ListTypes.COMPLETE) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle task list"
                )
            }
        },
        title = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Greetings.forHour(preferredName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onFilterClick
            ) {
                Icon(
                    imageVector = if (filterActive) Icons.Filled.FilterList else Icons.Outlined.FilterList,
                    contentDescription = "Filter tasks"
                )
            }
            IconButton(
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
fun MainTaskScreenBottomAppBar(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    BottomAppBar(
        actions = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onCalendarClick)
            ) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = "Calendar view"
                )
                Spacer(modifier = modifier.height(4.dp))
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onStatsClick)
            ) {
                Icon(Icons.Outlined.BarChart, contentDescription = "Stats view")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stats",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new task"
                )
            }
        }
    )
}

@Composable
fun TaskList(
    currentListType: ListTypes,
    incompleteTasks: List<Task>,
    completeTasks: List<Task>,
    allTasks: List<Task>,
    onTaskCompleted: (Task, Boolean) -> Unit,
    onTaskDeleted: (Task) -> Unit,
    onEditClick: (Task) -> Unit,
    onSubtaskCompleted: (Task, Boolean) -> Unit,
    onBreakdownClick: (Task) -> Unit,
    aiModelReady: (Boolean),
    modifier: Modifier = Modifier
) {
    val currentListItems = when (currentListType) {
        ListTypes.INCOMPLETE -> incompleteTasks
        ListTypes.COMPLETE -> completeTasks
    }

    if (currentListItems.isEmpty()) {
        val (icon, title, subtitle) = when (currentListType) {
            ListTypes.INCOMPLETE -> if (completeTasks.isEmpty())
                Triple(Icons.Outlined.Inbox, "Nothing here yet", "Add your first task with +")
            else Triple(
                Icons.Outlined.CheckCircle,
                "All caught up",
                "Everything's done. Nice work."
            )

            ListTypes.COMPLETE -> Triple(
                Icons.Outlined.HourglassEmpty,
                "Nothing completed yet",
                "Finished tasks will appear here"
            )
        }
        EmptyState(
            icon = icon, title = title, subtitle = subtitle,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        )
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(dimensionResource(R.dimen.padding_medium)),
        modifier = modifier
    ) {
        items(currentListItems, key = { it.id }) { task ->
            TaskCard(
                task = task,
                subtasks = allTasks.filter { it.parentTaskId == task.id },
                onTaskCompleted = onTaskCompleted,
                onTaskDeleted = onTaskDeleted,
                onEditClick = onEditClick,
                onSubtaskCompleted = onSubtaskCompleted,
                modifier = Modifier.animateItem(),
                onBreakdownClick = { onBreakdownClick(task) },
                isAiModelReady = aiModelReady

            )
        }
    }
}

@Composable
private fun FilterSheetContent(
    filterState: FilterState,
    onFilterChanged: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Sort by",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortOrder.entries.forEach { sort ->
                FilterChip(
                    selected = filterState.sortOrder == sort,
                    onClick = { onFilterChanged(filterState.copy(sortOrder = sort)) },
                    label = { Text(sort.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Category",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Category.entries.filter { it != Category.NONE }.forEach { category ->
                FilterChip(
                    selected = category in filterState.categories,
                    onClick = {
                        val updated = if (category in filterState.categories)
                            filterState.categories - category
                        else
                            filterState.categories + category
                        onFilterChanged(filterState.copy(categories = updated))
                    },
                    label = { Text(category.name.lowercase().replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Priority", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { priority ->
                FilterChip(
                    selected = priority in filterState.priorities,
                    onClick = {
                        val updated = if (priority in filterState.priorities)
                            filterState.priorities - priority
                        else
                            filterState.priorities + priority
                        onFilterChanged(filterState.copy(priorities = updated))
                    },
                    label = { Text("${priority.emoji} ${priority.label}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Duration", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Duration.entries.forEach { duration ->
                FilterChip(
                    selected = duration in filterState.durations,
                    onClick = {
                        val updated = if (duration in filterState.durations)
                            filterState.durations - duration
                        else
                            filterState.durations + duration
                        onFilterChanged(filterState.copy(durations = updated))
                    },
                    label = { Text(duration.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { onFilterChanged(FilterState()) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Clear all filters")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TaskListPreview() {
    TODOPrototypingTheme {
        TaskScreen(
            onNavigateToAddTask = { },
            onNavigateToSettings = { },
            onCalendarClick = { },
            onStatsClick = { },
            onEditClick = { }
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TaskListPreviewDark() {
    TODOPrototypingTheme(darkTheme = true) {
        TaskScreen(
            onNavigateToAddTask = { },
            onNavigateToSettings = { },
            onCalendarClick = { },
            onStatsClick = { },
            onEditClick = { }
        )
    }
}