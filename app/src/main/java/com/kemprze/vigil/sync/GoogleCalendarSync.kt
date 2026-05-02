package com.kemprze.vigil.sync
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
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
}