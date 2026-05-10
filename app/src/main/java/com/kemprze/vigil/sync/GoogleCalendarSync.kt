package com.kemprze.vigil.sync
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.client.util.DateTime
import java.time.LocalTime
import java.time.ZoneId
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.kemprze.vigil.data.model.SimpleTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleCalendarSync {
    fun getGoogleSignInClient(context: Context) = GoogleSignIn.getClient(
            context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar"))
            .build()
    )

    fun isSignedIn(context: Context) : Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun getCalendarService(context: Context) : com.google.api.services.calendar.Calendar? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf("https://www.googleapis.com/auth/calendar"))
            .also { it.selectedAccount = account.account }

        return com.google.api.services.calendar.Calendar.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Vigil")
            .build()
    }

    suspend fun setupVigilCalendar(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val service = getCalendarService(context) ?: return@withContext null
                val calendars = service.calendarList().list().execute()
                val existing = calendars.items?.find { it.summary == "Vigil"}

                if (existing != null) return@withContext existing.id
                val newCalendar = com.google.api.services.calendar.model.Calendar().apply {
                    summary = "Vigil"
                    description = "Tasks synced from Vigil app"
                }

                service.calendars().insert(newCalendar).execute().id
            } catch (e: Exception) {
                android.util.Log.d("VIGILSync", "setupVigilCalendar failed: ${e.message}")
                null
            }
        }
    }

    suspend fun syncTaskToCalendar(context: Context, task: SimpleTask, calendarId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val service = getCalendarService(context) ?: return@withContext null

                if (task.dueDate == null) return@withContext null


                val event = Event().apply {
                    summary = task.taskName
                    description = task.taskDescription.ifEmpty { null }

                    reminders = Event.Reminders().apply {
                        useDefault = false
                        overrides = listOf()
                    }
                }

                val zoneId = ZoneId.systemDefault()
                val dueDate = task.dueDate ?: return@withContext null
                val hasTime = dueDate.toLocalTime() != LocalTime.MIDNIGHT


                if (hasTime) {
                    val startMillis = dueDate.atZone(zoneId).toInstant().toEpochMilli()
                    val endMillis = dueDate.plusMinutes(task.duration.maxMinutes.toLong()
                        .coerceAtMost(120L)).atZone(zoneId).toInstant().toEpochMilli()

                    event.start = EventDateTime().setDateTime(DateTime(startMillis)).setTimeZone(zoneId.id)
                    event.end = EventDateTime().setDateTime(DateTime(endMillis)).setTimeZone(zoneId.id)
                } else {
                    val dateStr = dueDate.toLocalDate().toString()
                    event.start = EventDateTime().setDate(DateTime(dateStr))
                    event.end = EventDateTime().setDate(DateTime(dateStr))
                }

                service.events().insert(calendarId, event).execute().id
            } catch (e: Exception) {
                android.util.Log.d("VIGILSync", "setTaskToCalendar failed: ${e.message}")
                null
            }
        }
    }
}