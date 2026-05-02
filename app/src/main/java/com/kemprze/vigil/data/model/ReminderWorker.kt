package com.kemprze.vigil.data.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context,
                     params: WorkerParameters
): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val taskName = inputData.getString("task_name") ?: "Task reminder"
        val taskID = inputData.getString("task_id") ?: "Task ID"

        val notification = NotificationCompat.Builder(applicationContext, "task_reminders")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Reminder")
            .setContentText(taskName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(applicationContext)
                .notify(taskID.hashCode(), notification)
        }

        return Result.success()
    }
}