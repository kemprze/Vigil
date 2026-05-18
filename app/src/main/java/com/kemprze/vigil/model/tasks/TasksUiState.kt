package com.kemprze.vigil.model.tasks

import com.kemprze.vigil.data.model.FilterState
import com.kemprze.vigil.data.model.SimpleTask

data class TasksUiState(
    val tasks: List<SimpleTask> = emptyList(),
    val completedTasks: List<SimpleTask> = emptyList(),
    val isLoading: Boolean = false,
    val filterState: FilterState = FilterState()
)
