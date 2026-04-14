package com.kemprze.todoprototyping.data.model

enum class SortOrder(val label: String) {
    DUE_DATE("Due date"),
    PRIORITY("Priority"),
    CREATED("Created"),
    NAME("Name"),
    DURATION("Duration")
}

data class FilterState(
    val sortOrder: SortOrder = SortOrder.DUE_DATE,
    val categories: Set<Category> = emptySet(),
    val priorities: Set<Priority> = emptySet(),
    val durations: Set<Duration> = emptySet()
) {
    val isActive: Boolean
        get() = categories.isNotEmpty() || priorities.isNotEmpty() || durations.isNotEmpty()
}