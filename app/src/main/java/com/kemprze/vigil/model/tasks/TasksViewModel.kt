package com.kemprze.vigil.model.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kemprze.vigil.data.SettingsDataStore
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Duration
import com.kemprze.vigil.data.model.FilterState
import com.kemprze.vigil.data.model.Priority
import com.kemprze.vigil.data.model.ReminderWorker
import com.kemprze.vigil.data.model.SimpleTask
import com.kemprze.vigil.data.model.SortOrder
import com.kemprze.vigil.data.repository.TaskRepository
import com.kemprze.vigil.sync.GoogleCalendarSync
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class TasksViewModel(private val taskRepository: TaskRepository,
                     application: Application
): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TasksUiState())
    private var _allTasks: List<SimpleTask> = emptyList()
    private var _currentFilter: FilterState = FilterState()
    private val settingsDataStore = SettingsDataStore(getApplication())
    private val context = getApplication<Application>()
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getTasks().collect { allTasks ->
                _allTasks = allTasks
                applyFilterAndUpdate()
            }
        }
    }

    fun getTaskById(id: String): SimpleTask? {
        val task = _allTasks.find { it.id == id }
        return task
    }

    private fun applyFilterAndUpdate() {
        val filtered = _allTasks
            .filter { _currentFilter.categories.isEmpty() || it.category in _currentFilter.categories }
            .filter { _currentFilter.priorities.isEmpty() || it.priority in _currentFilter.priorities }
            .filter { _currentFilter.durations.isEmpty() || it.duration in _currentFilter.durations }
            .let { tasks ->
                when (_currentFilter.sortOrder) {
                    SortOrder.DUE_DATE -> tasks.sortedWith(compareBy(nullsLast()) { it.dueDate } )
                    SortOrder.PRIORITY -> tasks.sortedBy { it.priority }
                    SortOrder.CREATED -> tasks.sortedWith(compareBy(nullsLast()) { it.createdOn })
                    SortOrder.NAME -> tasks.sortedBy { it.taskName }
                    SortOrder.DURATION -> tasks.sortedBy { it.duration }
                }
            }

        val parentOnly = filtered.filter {
            it.parentTaskId == null
        }

        _uiState.update { currentState ->
            currentState.copy(
                tasks = parentOnly.filter { !it.isCompleted },
                completedTasks = filtered.filter { it.isCompleted },
                allTasks = _allTasks,
                isLoading = false,
                filterState = _currentFilter
            )
        }
    }

    fun onFilterChanged(filterState: FilterState) {
        _currentFilter = filterState
        applyFilterAndUpdate()
    }
    fun onTaskAdded(taskName: String,
                    taskDescription: String,
                    priority: Priority,
                    dueDate: LocalDateTime?,
                    needsReminder: Boolean,
                    remindMe: LocalDateTime?,
                    category: Category,
                    duration: Duration
    ) {
        val newTask = SimpleTask(
            taskName = taskName,
            taskDescription = taskDescription,
            priority = priority,
            dueDate = dueDate,
            remindMe = if (needsReminder) remindMe else null,
            createdOn = LocalDateTime.now(),
            category = category,
            isCompleted = false,
            duration = duration
        )

        viewModelScope.launch {
            taskRepository.insertTask(newTask)

            val calendarId = settingsDataStore.googleSyncFlow.first()

            if (calendarId != null && newTask.dueDate != null) {
                val eventId = GoogleCalendarSync.syncTaskToCalendar(context, newTask, calendarId)

                if (eventId != null) {
                    taskRepository.updateTask(newTask.copy(googleCalendarEventId = eventId))
                }
            }

            if (needsReminder && newTask.remindMe != null) {
                val delay = ChronoUnit.MILLIS.between(
                    LocalDateTime.now(),
                    newTask.remindMe
                )

                val inputData = Data.Builder()
                    .putString("task_name", newTask.taskName)
                    .putString("task_id", newTask.id)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(getApplication<Application>())
                    .enqueue(workRequest)
            }
        }
    }

    fun onTaskCompleted(task: SimpleTask, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isCompleted = isCompleted))
        }
    }

    fun onTaskUpdated(task: SimpleTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task)

            val calendarId = settingsDataStore.googleSyncFlow.first()
            val eventId = task.googleCalendarEventId


            if (calendarId != null && eventId != null && task.dueDate != null) {
                eventId.let { id ->
                    GoogleCalendarSync.updateCalendarEvent(context, task, calendarId,id)
                }
            }
        }
    }

    fun onTaskDeleted(task: SimpleTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)

            val calendarId = settingsDataStore.googleSyncFlow.first()
            val eventId = task.googleCalendarEventId

            if (calendarId != null && eventId != null) {
                eventId.let { id ->
                    GoogleCalendarSync.deleteCalendarEvent(context, calendarId, id)
                }
            }
        }
    }

    fun getSubtasksForTask(parentId: String): Flow<List<SimpleTask>> {
        return taskRepository.getSubtasksForTask(parentId)
    }

    fun onSubtaskAdded(parentTask: SimpleTask, subtaskName: String) {
        val subtaskOrder = _allTasks.count { it.parentTaskId == parentTask.id }

        val subtask = SimpleTask(
            taskName = subtaskName,
            parentTaskId = parentTask.id,
            subtaskOrder = subtaskOrder,
            priority = parentTask.priority,
            category = parentTask.category,
            duration = Duration.SHORT
        )

        viewModelScope.launch {
            taskRepository.insertTask(subtask)
        }
    }

}