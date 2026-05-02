package com.kemprze.vigil.model.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Duration
import com.kemprze.vigil.data.model.FilterState
import com.kemprze.vigil.data.model.Priority
import com.kemprze.vigil.data.model.ReminderWorker
import com.kemprze.vigil.data.model.SortOrder
import com.kemprze.vigil.data.model.simpleTask
import com.kemprze.vigil.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class TasksUiState(
    val tasks: List<simpleTask> = emptyList(),
    val completedTasks: List<simpleTask> = emptyList(),
    val isLoading: Boolean = false,
    val filterState: FilterState = FilterState()
)
class TasksViewModel(private val taskRepository: TaskRepository,
                     application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TasksUiState())
    private var _allTasks: List<simpleTask> = emptyList()
    private var _currentFilter: FilterState = FilterState()
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

    fun getTaskById(id: String): simpleTask? {
        return _allTasks.find { it.id == id }
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

        _uiState.update { currentState ->
            currentState.copy(
                tasks = filtered.filter { !it.isCompleted },
                completedTasks = filtered.filter { it.isCompleted },
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
                    duration: Duration) {
        val newTask = simpleTask(
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

            if (needsReminder && newTask.remindMe != null) {
                val delay = java.time.temporal.ChronoUnit.MILLIS.between(
                    LocalDateTime.now(),
                    newTask.remindMe
                )

                val inputData = androidx.work.Data.Builder()
                    .putString("task_name", newTask.taskName)
                    .putString("task_id", newTask.id)
                    .build()

                val workRequest = androidx.work.OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                androidx.work.WorkManager.getInstance(getApplication<Application>())
                    .enqueue(workRequest)
            }
        }
    }

    fun onTaskCompleted(task: simpleTask, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isCompleted = isCompleted))
        }
    }

    fun onTaskUpdated(task: simpleTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun onTaskDeleted(task: simpleTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

}