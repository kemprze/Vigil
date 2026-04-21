package com.kemprze.todoprototyping.data.model

enum class Priority(val level: Int, val label: String, val emoji: String) {
    CRITICAL(1, "Has to happen", "🔴"),
    SIGNIFICANT(2, "Really matters", "🟠"),
    NORMAL(3, "Should get done", "🟡"),
    RELAXED(4, "When I get to it", "🟢"),
    SOMEDAY(5, "Someday maybe", "⚪")
}