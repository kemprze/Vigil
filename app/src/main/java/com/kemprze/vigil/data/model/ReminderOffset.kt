package com.kemprze.vigil.data.model

import java.time.LocalDateTime

enum class ReminderOffset(val label: String) {
    ONE_HOUR("1 hour before") {
        override fun calculateReminderTime(dueDate: LocalDateTime): LocalDateTime = dueDate.minusHours(1)
    },
    ONE_DAY("1 day before") {
        override fun calculateReminderTime(dueDate: LocalDateTime): LocalDateTime = dueDate.minusDays(1).withHour(9).withMinute(0)
    },
    THREE_DAYS("3 days before") {
        override fun calculateReminderTime(dueDate: LocalDateTime): LocalDateTime = dueDate.minusDays(3).withHour(9).withMinute(0)
    },
    ONE_WEEK("1 week before") {
        override fun calculateReminderTime(dueDate: LocalDateTime): LocalDateTime = dueDate.minusDays(7).withHour(9).withMinute(0)
    };

    abstract fun calculateReminderTime(dueDate: LocalDateTime): LocalDateTime
}