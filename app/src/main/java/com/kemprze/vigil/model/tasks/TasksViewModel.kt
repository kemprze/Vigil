package com.kemprze.vigil.model.tasks

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kemprze.vigil.ai.DownloadModelWorker
import com.kemprze.vigil.ai.LocalInferenceEngine
import com.kemprze.vigil.data.SettingsDataStore
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Duration
import com.kemprze.vigil.data.model.FilterState
import com.kemprze.vigil.data.model.Priority
import com.kemprze.vigil.data.model.ReminderWorker
import com.kemprze.vigil.data.model.Task
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class TasksViewModel(
    private val taskRepository: TaskRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TasksUiState())
    private var _allTasks: List<Task> = emptyList()
    private var _currentFilter: FilterState = FilterState()
    private var engineMutex = Mutex()
    private val settingsDataStore = SettingsDataStore(getApplication())
    private val context = getApplication<Application>()
    private val inferenceEngine = LocalInferenceEngine(getApplication())
    private val _suggestedSubtasks = MutableStateFlow<List<String>>(emptyList())
    val suggestedSubtasks = _suggestedSubtasks.asStateFlow()
    private val _isBreakingDown = MutableStateFlow(false)
    val isBreakingDown = _isBreakingDown.asStateFlow()
    private val _currentBreakdownTask = MutableStateFlow<Task?>(null)
    val currentBreakdownTask = _currentBreakdownTask.asStateFlow()
    private var _insight = MutableStateFlow<String>("")
    private val _isGeneratingInsight = MutableStateFlow<Boolean>(false)
    val insight = _insight.asStateFlow()
    val isGeneratingInsight = _isGeneratingInsight.asStateFlow()
    val aiModelReadyFlow = settingsDataStore.aiModelReadyFlow
    private var lastInsightSnapshot: Int = -1


    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {

        loadTasks()
    }

    override fun onCleared() {
        super.onCleared()
        inferenceEngine.close()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getTasks().collect { allTasks ->
                _allTasks = allTasks
                applyFilterAndUpdate()
            }
        }
    }

    fun getTaskById(id: String): Task? {
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
                    SortOrder.DUE_DATE -> tasks.sortedWith(compareBy(nullsLast()) { it.dueDate })
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
                completedTasks = parentOnly.filter { it.isCompleted },
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

    fun onTaskAdded(
        taskName: String,
        taskDescription: String = "",
        priority: Priority = Priority.NORMAL,
        dueDate: LocalDateTime? = null,
        needsReminder: Boolean = false,
        remindMe: LocalDateTime? = null,
        category: Category = Category.NONE,
        duration: Duration = Duration.MEDIUM
    ) {
        val newTask = Task(
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

    fun onTaskCompleted(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isCompleted = isCompleted))


            val parentId = task.parentTaskId ?: return@launch
            val siblings = _allTasks.filter {
                it.parentTaskId == parentId
            }

            val allDone = siblings.all {
                if (it.id == task.id) isCompleted else it.isCompleted
            }

            val parent = _allTasks.find { it.id == parentId } ?: return@launch
            taskRepository.updateTask(parent.copy(isCompleted = allDone))
        }
    }

    fun onTaskUpdated(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)

            val calendarId = settingsDataStore.googleSyncFlow.first()
            val eventId = task.googleCalendarEventId


            if (calendarId != null && eventId != null && task.dueDate != null) {
                eventId.let { id ->
                    GoogleCalendarSync.updateCalendarEvent(context, task, calendarId, id)
                }
            }
        }
    }

    fun onTaskDeleted(task: Task) {
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

    fun getSubtasksForTask(parentId: String): Flow<List<Task>> {
        return taskRepository.getSubtasksForTask(parentId)
    }

    fun onSubtaskAdded(parentTask: Task, subtaskName: String) {
        val subtaskOrder = _allTasks.count { it.parentTaskId == parentTask.id }

        val subtask = Task(
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

    fun breakdownTask(task: Task) {
        android.util.Log.d("InferenceEngine", "breakdownTask called for: ${task.taskName}")
        _currentBreakdownTask.value = task
        viewModelScope.launch {
            _isBreakingDown.value = true
            val variant = settingsDataStore.aiModelVariantFlow.first()
            val modelPath = DownloadModelWorker.modelFile(getApplication(), variant).absolutePath


            if (!inferenceEngine.isReady()) {
                inferenceEngine.initialize(modelPath)
            }

            _suggestedSubtasks.value = inferenceEngine.suggestSubtasks(
                taskName = task.taskName,
                taskDescription = task.taskDescription,
                feedbackStyle = settingsDataStore.feedbackStyleFlow.first()
            )
            _isBreakingDown.value = false

        }

    }

    fun generateInsight(tasks: List<Task>, completedTasks: List<Task>) {
        val completedTasksCount = completedTasks.count()
        val totalTasksCount = tasks.count() + completedTasks.count()
        val incompleteTasksCount = tasks.count()
        val currentSnapshot = (tasks + completedTasks).hashCode()

        val groupedCategories = (tasks + completedTasks).groupBy { it.category }
        val maxCount = groupedCategories.maxOfOrNull { it.value.size } ?: 0
        val topCategory = if (groupedCategories.count { it.value.size == maxCount} > 1) {
            "multiple categories equally"
        } else {
            groupedCategories.maxByOrNull { it.value.size }?.key?.name ?: "None"
        }

        if (lastInsightSnapshot == currentSnapshot) {
            return
        }

        viewModelScope.launch {
            _isGeneratingInsight.value = true
            val variant = settingsDataStore.aiModelVariantFlow.first()
            val modelPath =
                DownloadModelWorker.modelFile(getApplication(), variant).absolutePath


            if (!inferenceEngine.isReady()) {
                inferenceEngine.initialize(modelPath)
            }

            _insight.value = inferenceEngine.generateInsight(
                totalTasksCount,
                completedTasksCount,
                incompleteTasksCount,
                topCategory,
                feedbackStyle = settingsDataStore.feedbackStyleFlow.first()
            )
            lastInsightSnapshot = currentSnapshot
            _isGeneratingInsight.value = false

        }


    }

    fun clearSuggestedSubtasks() {
        _suggestedSubtasks.value = emptyList()
    }

    suspend fun suggestCategory(taskName: String): Category {
        val variant = settingsDataStore.aiModelVariantFlow.first()
        val modelPath = DownloadModelWorker.modelFile(getApplication(), variant).absolutePath


        if (!inferenceEngine.isReady()) {
            inferenceEngine.initialize(modelPath)
        }
        val categorySuggested = Category.entries.find {
            inferenceEngine.suggestCategory(taskName) == it.name
        } ?: Category.NONE

        return categorySuggested

    }
}