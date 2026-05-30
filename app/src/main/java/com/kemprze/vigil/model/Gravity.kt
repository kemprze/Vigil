package com.kemprze.vigil.model

import com.kemprze.vigil.data.model.Task
import java.time.LocalDate

fun gravityScore(task: Task): Float {
    val priorityScore = 6 - task.priority.level

    val durationScore = when {
        task.duration.maxMinutes <= 15 -> 3
        task.duration.maxMinutes <= 60 -> 2
        task.duration.maxMinutes <= 120 -> 1
        else -> 0
    }

    val today = LocalDate.now()
    val dueDate = task.dueDate

    val dueDateScore = when {
        dueDate == null -> 0
        dueDate.toLocalDate().isBefore(today) -> 5
        dueDate.toLocalDate() == today -> 4
        dueDate.toLocalDate() == today.plusDays(1) -> 3
        dueDate.toLocalDate().isBefore(today.plusDays(7)) -> 2
        else -> 1
    }

    return (priorityScore + durationScore + dueDateScore).toFloat()
}