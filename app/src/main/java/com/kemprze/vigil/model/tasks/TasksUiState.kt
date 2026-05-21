package com.kemprze.vigil.model.tasks

import com.kemprze.vigil.data.model.FilterState
import com.kemprze.vigil.data.model.Task

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filterState: FilterState = FilterState()
)
