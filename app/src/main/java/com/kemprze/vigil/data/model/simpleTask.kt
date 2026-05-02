package com.kemprze.vigil.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class simpleTask(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var taskName: String,
    var taskDescription: String = "",
    var priority: Priority = Priority.NORMAL,
    var dueDate: LocalDateTime? = null,
    var needsReminder: Boolean = false,
    var remindMe: LocalDateTime? = null,
    var reminderOffset: ReminderOffset? = null,
    var createdOn: LocalDateTime? = LocalDateTime.now(),
    var category: Category = Category.NONE,
    var isCompleted: Boolean = false,
    var duration: Duration = Duration.MEDIUM
)