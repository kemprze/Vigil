# Vigil — Android Concepts Reference

A living document covering the core concepts behind how Vigil is built.
Every concept is grounded in code from this project, so you can cross-reference
as you read. Written for someone who has already been writing the code and wants
to understand *why* it works, not just that it does.

---

## Table of Contents

1. [The Architecture — Layers Overview](#1-the-architecture--layers-overview)
2. [Kotlin Foundations](#2-kotlin-foundations)
3. [Coroutines and Flow](#3-coroutines-and-flow)
4. [Room — The Local Database](#4-room--the-local-database)
5. [Repository Pattern](#5-repository-pattern)
6. [ViewModel and StateFlow](#6-viewmodel-and-stateflow)
7. [Jetpack Compose — UI and State](#7-jetpack-compose--ui-and-state)
8. [DataStore — Persistent Preferences](#8-datastore--persistent-preferences)
9. [WorkManager — Background Tasks](#9-workmanager--background-tasks)
10. [Navigation](#10-navigation)
11. [Google Calendar Integration](#11-google-calendar-integration)
12. [How It All Connects](#12-how-it-all-connects)

---

## 1. The Architecture — Layers Overview

Vigil follows **MVVM** (Model–View–ViewModel), which is the standard Android
architecture pattern. The idea is simple: keep concerns separated so that each
part of the app has one job.

```
UI Layer           →   Composables (SettingsScreen, TaskCard, AddTaskWizard...)
        ↕
ViewModel Layer    →   TasksViewModel, SettingsViewModel
        ↕
Data Layer         →   TaskRepository, TaskDao, SettingsDataStore, GoogleCalendarSync
        ↕
Persistence        →   Room (SQLite), DataStore (key-value), Google Calendar API
```

**Why this matters:**
- The UI never talks to the database directly. It talks to the ViewModel.
- The ViewModel never knows which *database* it's using. It talks to the Repository.
- The Repository talks to wherever data actually lives (Room, network, etc.).

This means you can swap out Room for a different database without touching the UI.
Or write unit tests for the ViewModel without needing a real database.

---

## 2. Kotlin Foundations

### Data classes

```kotlin
data class SimpleTask(
    val id: String = UUID.randomUUID().toString(),
    var taskName: String,
    var isCompleted: Boolean = false,
    // ...
)
```

A `data class` automatically generates `equals()`, `hashCode()`, `toString()`,
and most importantly: **`copy()`**.

`copy()` is central to how you update state safely:
```kotlin
// Don't mutate directly. Make a new copy with the changed field:
val updated = task.copy(isCompleted = true)
```

This is immutability-friendly programming — the original `task` is untouched.

### Companion objects

```kotlin
companion object {
    val THEME_KEY = stringPreferencesKey("app_theme")
}
```

A `companion object` is Kotlin's equivalent of Java's `static`. It belongs to
the class itself, not to any instance. Used in `SettingsDataStore` to define
preference keys once and share them across the class.

### Enums

```kotlin
enum class Priority { LOW, NORMAL, HIGH }
enum class Category { NONE, WORK, PERSONAL, HEALTH, LEARNING }
```

Enums are named constants. They're stored in Room as their `.name` string
("LOW", "NORMAL") via a `TypeConverter`. When you read them back, you call
`Priority.valueOf("NORMAL")` to convert the string back to the enum.

### Nullable types

Kotlin distinguishes between a value that exists (`String`) and one that might
not exist (`String?`). The `?` suffix means "this can be null."

```kotlin
var dueDate: LocalDateTime? = null       // might have a date, might not
var googleCalendarEventId: String? = null // null = never synced
```

To use a nullable safely:
```kotlin
if (calendarId != null) { /* now calendarId is treated as non-null inside */ }
calendarId?.let { id -> /* runs only if id is non-null */ }
calendarId ?: "default"  // use "default" if calendarId is null
```

---

## 3. Coroutines and Flow

This is probably the most important conceptual area to understand deeply.

### The problem coroutines solve

Old Android code did network/database calls on background threads using
callbacks or RxJava. Coroutines let you write that same code in a
straightforward, sequential style — without blocking the main thread.

### suspend functions

```kotlin
suspend fun insertTask(task: SimpleTask) {
    taskDao.insertTask(task)
}
```

A `suspend` function can be *paused* while waiting for something (like a DB
write) and *resumed* when it's done, without blocking the thread. You can only
call a `suspend` function from inside a coroutine or another `suspend` function.

### Coroutine scope and launch

```kotlin
viewModelScope.launch {
    taskRepository.insertTask(newTask)
}
```

`viewModelScope` is a scope tied to the ViewModel's lifecycle — when the
ViewModel is destroyed, all coroutines in its scope are automatically cancelled.
`launch` starts a new coroutine. Code inside runs sequentially, top to bottom,
even though it may involve pausing.

### Flow

A `Flow` is a stream of values over time. Think of it like a pipe:

```kotlin
// In TaskDao:
fun getAllTasks(): Flow<List<SimpleTask>>
```

This doesn't return a list once. It returns a stream that emits a new list
every time the `tasks` table changes. The UI subscribes to this stream and
updates automatically.

**Flow vs suspend:**
- `suspend fun` → do something once, wait for the result
- `Flow` → keep receiving results as they change over time

### StateFlow vs MutableStateFlow

```kotlin
private val _uiState = MutableStateFlow(TasksUiState())
val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()
```

`MutableStateFlow` is a Flow that holds a single current value and lets you
update it. The ViewModel keeps the mutable version private and exposes a
read-only `StateFlow` to the UI. This prevents the UI from directly mutating
state — it has to go through the ViewModel.

### update() and copy()

```kotlin
_uiState.update { currentState ->
    currentState.copy(tasks = filtered, isLoading = false)
}
```

`update` atomically reads the current value and sets the new one. `copy` creates
a modified version of the data class. Together they're the idiomatic way to
update UI state in Android.

### Dispatchers

```kotlin
withContext(Dispatchers.IO) {
    // network/database call here
}
```

Coroutines run on a dispatcher, which controls which thread pool they use:
- `Dispatchers.Main` — the UI thread (only for UI updates)
- `Dispatchers.IO` — optimized for I/O: network, database, file operations
- `Dispatchers.Default` — CPU-intensive work

Room automatically moves its operations off the main thread, so you usually
don't need to specify the dispatcher for Room calls. For network calls (like
the Calendar API), you do.

---

## 4. Room — The Local Database

Room is a type-safe wrapper around SQLite. You define your schema in Kotlin
and Room generates the SQL.

### The three core pieces

**@Entity — the table:**
```kotlin
@Entity(tableName = "tasks")
data class SimpleTask(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var taskName: String,
    var isCompleted: Boolean = false,
    // ...
)
```
Each `@Entity` class maps to a database table. Each property maps to a column.
`@PrimaryKey` marks the unique identifier.

**@Dao — the queries:**
```kotlin
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<SimpleTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: SimpleTask)

    @Update
    suspend fun updateTask(task: SimpleTask)

    @Delete
    suspend fun deleteTask(task: SimpleTask)
}
```
The DAO (Data Access Object) is an interface. Room generates the implementation.
You annotate methods with what SQL operation they perform. Notice:
- `getAllTasks()` returns a `Flow` — Room automatically re-emits when data changes
- Write operations are `suspend` functions — they run off the main thread
- `OnConflictStrategy.REPLACE` means if you insert with the same primary key,
  it replaces the existing row — useful for upsert behaviour

**@Database — the entry point:**
```kotlin
@Database(entities = [SimpleTask::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                // create instance if not exists
            }
        }
    }
}
```

The `@Database` annotation lists all entities and the current schema version.
The singleton pattern (`@Volatile`, `synchronized`) ensures only one instance
of the database exists at a time — important because SQLite doesn't handle
multiple connections well.

### TypeConverters

Room can only store primitive types (String, Int, Boolean, etc.) natively.
For custom types like `LocalDateTime` or `Priority`, you need to tell Room how
to convert them:

```kotlin
class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(name: String): Priority = Priority.valueOf(name)

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? =
        dateTime?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }
}
```

Room calls these automatically when reading/writing. From Room's perspective,
a `Priority` column is just a `TEXT` column containing "LOW", "NORMAL", or "HIGH".

### Migrations

When you change the database schema (add/remove columns, rename tables), you
need a `Migration` that tells Room how to transform existing data:

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN googleCalendarEventId TEXT")
        db.execSQL("ALTER TABLE tasks ADD COLUMN parentTaskId TEXT")
        db.execSQL("ALTER TABLE tasks ADD COLUMN subtaskOrder INTEGER")
    }
}
```

Without migrations, Room will throw an error or (if you use
`fallbackToDestructiveMigration()`) wipe the user's data. Always write
migrations when bumping the version number.

---

## 5. Repository Pattern

```kotlin
class TaskRepository(private val taskDao: TaskDao) {
    fun getTasks(): Flow<List<SimpleTask>> = taskDao.getAllTasks()
    suspend fun insertTask(task: SimpleTask) = taskDao.insertTask(task)
    suspend fun updateTask(task: SimpleTask) = taskDao.updateTask(task)
    suspend fun deleteTask(task: SimpleTask) = taskDao.deleteTask(task)
}
```

Right now the Repository looks like a thin pass-through. That's fine — it's
doing its job even when there's no logic in it. The value becomes clear when:

- You add a **second data source** (e.g., sync to Google Calendar, cache in
  memory). The Repository decides which source to use and how to merge them.
  The ViewModel doesn't care.
- You want to add **business logic** around reads/writes (e.g., don't insert
  a task if it's a duplicate). That goes here, not in the ViewModel.
- You want to **test the ViewModel** in isolation by swapping in a fake
  repository.

---

## 6. ViewModel and StateFlow

```kotlin
class TasksViewModel(
    private val taskRepository: TaskRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }
}
```

**Why AndroidViewModel instead of ViewModel?**
`AndroidViewModel` gives you access to the Application context via
`getApplication<Application>()`. Used in `TasksViewModel` to access
`WorkManager` and `SettingsDataStore` when creating tasks. Regular `ViewModel`
has no context — which is cleaner, but sometimes you need it.

**Lifecycle awareness:**
The ViewModel survives configuration changes (screen rotation). The UI is
destroyed and recreated, but the ViewModel keeps running. This is why state
lives in the ViewModel, not in composables.

**The state data class:**
```kotlin
data class TasksUiState(
    val tasks: List<SimpleTask> = emptyList(),
    val completedTasks: List<SimpleTask> = emptyList(),
    val isLoading: Boolean = false,
    val filterState: FilterState = FilterState()
)
```

A single data class represents everything the UI needs to know. The UI observes
this and re-renders whenever it changes. One source of truth.

---

## 7. Jetpack Compose — UI and State

Compose is declarative: you describe *what* the UI should look like given the
current state, and Compose figures out *what to redraw* when state changes.

### @Composable

A `@Composable` function is a function that can emit UI. It can call other
composables. It runs (and re-runs) whenever its inputs change — this is called
**recomposition**.

### remember and mutableStateOf

```kotlin
var taskName by remember { mutableStateOf("") }
```

`mutableStateOf` creates a state holder that triggers recomposition when its
value changes. `remember` keeps the value alive across recompositions (but not
across navigation or process death).

`by` uses Kotlin's property delegation — `taskName` reads and writes directly
to the state holder without needing `.value`.

### rememberSaveable

```kotlin
var dueTimeHour by rememberSaveable { mutableStateOf<Int?>(null) }
```

Like `remember`, but also survives process death and configuration changes by
saving to the saved state registry. Use it for user input in forms.

### collectAsState

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

Converts a `StateFlow` (or any `Flow`) into Compose state. When the Flow emits
a new value, the composable recomposes. This is the bridge between the ViewModel
world (Flow) and the Compose world (State).

### State hoisting

The pattern of keeping state at the highest level that needs it, and passing it
down as parameters. In Vigil, the ViewModel holds state that multiple screens
need. Individual composables hold local state (like "is this dropdown open?")
with `remember`.

---

## 8. DataStore — Persistent Preferences

DataStore is the modern replacement for `SharedPreferences`. It's type-safe,
coroutine-based, and uses Flow for reads.

```kotlin
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

This creates a single DataStore for the app (delegated extension property —
only one instance is created no matter how many times you call `context.dataStore`).

**Keys:**
```kotlin
val THEME_KEY = stringPreferencesKey("app_theme")
val IS_DYNAMIC_COLOR = booleanPreferencesKey("is_dynamic_color")
val GOOGLE_CALENDAR_SYNC_ID = stringPreferencesKey("google_calendar_id")
```

Keys are typed — a `stringPreferencesKey` stores a `String`, a
`booleanPreferencesKey` stores a `Boolean`. This is the type-safety advantage
over `SharedPreferences`.

**Reading (returns a Flow):**
```kotlin
val googleSyncFlow: Flow<String?> = context.dataStore.data.map {
    prefs -> prefs[GOOGLE_CALENDAR_SYNC_ID]
}
```

`context.dataStore.data` is a `Flow<Preferences>`. You map it to extract the
value you want. Because it's a Flow, the UI automatically updates if you change
the value elsewhere.

For one-shot reads (e.g., inside `onTaskAdded`):
```kotlin
val calendarId = settingsDataStore.googleSyncFlow.first()
```

`.first()` collects the first emission and cancels the subscription — gives you
the current value without ongoing observation.

**Writing:**
```kotlin
suspend fun saveGoogleCalendarId(id: String) {
    context.dataStore.edit { prefs -> prefs[GOOGLE_CALENDAR_SYNC_ID] = id }
}
```

`edit` opens a transaction. Changes are atomic — either all are applied or none.

---

## 9. WorkManager — Background Tasks

WorkManager schedules deferrable background work that needs to run even if the
app is closed.

In Vigil, it handles reminders:
```kotlin
val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
    .setInputData(
        Data.Builder()
            .putString("task_name", newTask.taskName)
            .putString("task_id", newTask.id)
            .build()
    )
    .build()

WorkManager.getInstance(context).enqueue(workRequest)
```

`ReminderWorker` implements `Worker` and does the actual notification work.
The `Data` object passes parameters to the worker. `setInitialDelay` delays
execution until the reminder time.

WorkManager survives app restarts — Android will re-schedule the work after
a reboot.

---

## 10. Navigation

Vigil uses Compose Navigation with a `NavHost`:

```kotlin
// Route definitions (Screen.kt):
object EditTaskScreen : Screen("edit_task_screen/{task_id}") {
    fun createRoute(taskId: String) = "edit_task_screen/$taskId"
}
```

Routes are URL-like strings. `{task_id}` is a placeholder for an argument.
Snake_case is the Compose Navigation convention for argument names.

**Navigating with an argument:**
```kotlin
navController.navigate(Screen.EditTaskScreen.createRoute(task.id))
```

**Receiving the argument:**
```kotlin
composable(
    route = Screen.EditTaskScreen.route,
    arguments = listOf(navArgument("task_id") { type = NavType.StringType })
) { backStackEntry ->
    val taskId = backStackEntry.arguments?.getString("task_id") ?: return@composable
    EditTaskScreen(taskId = taskId, ...)
}
```

The `NavBackStackEntry` holds the arguments. You extract them and pass to the
composable as regular parameters.

---

## 11. Google Calendar Integration

This is where Android app development meets external APIs.

### OAuth 2.0

The user doesn't give Vigil their Google password. Instead:
1. Vigil redirects to a Google-hosted login screen.
2. The user signs in and approves the requested permissions (scopes).
3. Google gives Vigil a token that proves the user approved access.
4. Vigil uses that token to make Calendar API calls on the user's behalf.

The token is stored by the Google Sign-In SDK automatically. On future runs,
`GoogleSignIn.getLastSignedInAccount(context)` returns the previously signed-in
account without re-prompting.

### Scopes

```kotlin
.requestScopes(Scope("https://www.googleapis.com/auth/calendar"))
```

A scope is a permission string that declares what your app will access. The
`calendar` scope covers reading/writing calendars and events. Requesting a
narrower scope (like `calendar.events`) would be blocked from accessing
`calendarList` — which is why the full scope is needed here.

### GoogleAccountCredential

```kotlin
val credential = GoogleAccountCredential
    .usingOAuth2(context, listOf("https://www.googleapis.com/auth/calendar"))
credential.selectedAccount = account.account

val service = Calendar.Builder(
    NetHttpTransport(),
    GsonFactory.getDefaultInstance(),
    credential
).setApplicationName("Vigil").build()
```

`GoogleAccountCredential` wraps the OAuth token and attaches it to every API
request. The `Calendar` object is the entry point to all Calendar API calls.

### Architecture decision: calendarId as source of truth

Rather than storing a boolean `isConnected`, Vigil stores the Google Calendar
ID in DataStore. `null` means not connected; a non-null string means connected
and is the ID needed for all API calls. This eliminates a possible inconsistency
between a boolean flag and the actual connection state.

---

## 12. How It All Connects

Here's the full flow when a user creates a task in Vigil:

```
1. User fills out AddTaskWizard and taps "Add"

2. Composable calls:
   tasksViewModel.onTaskAdded(taskName, dueDate, ...)

3. ViewModel (onTaskAdded):
   a. Creates a SimpleTask data class
   b. Calls taskRepository.insertTask(newTask)

4. Repository → DAO:
   taskDao.insertTask(task)
   Room writes to SQLite

5. Room emits the updated list via Flow
   → ViewModel's loadTasks() collector fires
   → _uiState.update { ... } called
   → UI recomposes with new task visible

6. Back in onTaskAdded (still in the same coroutine):
   a. Reads calendarId from DataStore
   b. If calendarId != null AND task has a dueDate:
      - Calls GoogleCalendarSync.syncTaskToCalendar(...)
      - Makes a Calendar API call (runs on Dispatchers.IO)
      - Gets back an event ID
      - Calls taskRepository.updateTask(newTask.copy(googleCalendarEventId = eventId))
      - Room stores the event ID for future sync operations

7. If reminder needed:
   - Schedules a OneTimeWorkRequest with WorkManager
   - WorkManager fires ReminderWorker at the reminder time
   - User gets a notification
```

The key insight is that **everything flows downward** (UI → ViewModel →
Repository → Database) and **reactive updates flow back upward** via Flow
(Database → ViewModel → UI). The UI never pulls data — it subscribes and
reacts.

---

*This document reflects Vigil as of May 2026 (Room v3, Google Calendar sync live).*
*Update it as the app grows.*
