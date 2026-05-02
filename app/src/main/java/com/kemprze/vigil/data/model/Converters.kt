package com.kemprze.vigil.data.model

import androidx.room.TypeConverter
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun localDateToString(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun categoryToString(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun stringToCategory(value: String): Category {
        return Category.valueOf(value)
    }

    @TypeConverter
    fun priorityToInt(priority: Priority): Int {
        return priority.level
    }

    @TypeConverter
    fun intToPriority(value: Int): Priority {
        return Priority.entries.first { it.level == value }
    }

    @TypeConverter
    fun durationToInt(duration: Duration): Int {
        return duration.ordinal
    }

    @TypeConverter
    fun intToDuration(value: Int): Duration {
        return Duration.entries.first { it.ordinal == value}
    }

    @TypeConverter
    fun reminderOffsetToInt(offset: ReminderOffset?): Int? {
        return offset?.ordinal
    }

    @TypeConverter
    fun intToReminderOffset(value: Int?): ReminderOffset? {
        return value?.let { ReminderOffset.entries[it] }
    }
}