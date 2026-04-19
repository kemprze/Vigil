package com.kemprze.todoprototyping

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kemprze.todoprototyping.data.DarkModePreferences
import com.kemprze.todoprototyping.model.AddTaskWizard
import com.kemprze.todoprototyping.model.CalendarScreen
import com.kemprze.todoprototyping.model.StatsScreen
import com.kemprze.todoprototyping.model.TaskScreen
import com.kemprze.todoprototyping.model.settings.SettingsScreen
import com.kemprze.todoprototyping.model.tasks.TasksViewModel
import com.kemprze.todoprototyping.model.tasks.TasksViewModelFactory
import com.kemprze.todoprototyping.model.settings.SettingsViewModel
import com.kemprze.todoprototyping.navigation.Screen
import com.kemprze.todoprototyping.ui.theme.AppTheme
import com.kemprze.todoprototyping.ui.theme.TODOPrototypingTheme
import com.kemprze.todoprototyping.ui.theme.AppFont

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_reminders",
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your scheduled tasks"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val appTheme by settingsViewModel.themeFlow.collectAsState(initial = AppTheme.SCARLET)
            val appFont by settingsViewModel.fontFlow.collectAsState(initial = AppFont.LATO)
            val darkMode by settingsViewModel.darkModeFlow.collectAsState(initial = DarkModePreferences.SYSTEM)
            val dynamicColor by settingsViewModel.dynamicColorFlow.collectAsState(initial = false)

            TODOPrototypingTheme(
                appTheme = appTheme,
                appFont = appFont,
                darkTheme = when (darkMode) {
                    DarkModePreferences.DARK -> true
                    DarkModePreferences.LIGHT -> false
                    DarkModePreferences.SYSTEM -> isSystemInDarkTheme()
                },
                dynamicColor = dynamicColor
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier,
                  settingsViewModel: SettingsViewModel) {
    val navController: NavHostController = rememberNavController()
    val tasksViewModel: TasksViewModel = viewModel(
        factory = TasksViewModelFactory(LocalContext.current)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.TaskListScreen.route,
        modifier = modifier
    ) {
        composable(route = Screen.TaskListScreen.route) {
            TaskScreen(
                tasksViewModel = tasksViewModel,
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddTaskScreen.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.SettingsScreen.route)
                },
                onCalendarClick = { navController.navigate(Screen.CalendarScreen.route) },
                onStatsClick = { navController.navigate(Screen.StatsScreen.route) }
            )
        }
        composable(route = Screen.AddTaskScreen.route) {
            AddTaskWizard(
                onNavigateBack = { navController.navigateUp() },
                onAddClick = { task ->
                    tasksViewModel.onTaskAdded(
                        taskName = task.taskName,
                        taskDescription = task.taskDescription,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        needsReminder = task.needsReminder,
                        remindMe = task.remindMe,
                        category = task.category,
                        duration = task.duration
                    )
                    navController.navigateUp()
                })
        }
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(route = Screen.CalendarScreen.route) {
            CalendarScreen(
                tasks = tasksViewModel.uiState.collectAsState().value.tasks + tasksViewModel.uiState.collectAsState().value.completedTasks,
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(route = Screen.StatsScreen.route) {
            StatsScreen(tasks = tasksViewModel.uiState.collectAsState().value.tasks,
                completedTasks = tasksViewModel.uiState.collectAsState().value.completedTasks,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

@Composable
@Preview
fun NavigationPreview() {
    TODOPrototypingTheme(appTheme = AppTheme.SCARLET, appFont = AppFont.ATKINSON) {
        AppNavigation(settingsViewModel = viewModel())
    }
}

@Composable
@Preview
fun NavigationPreviewDark() {
    TODOPrototypingTheme(appTheme = AppTheme.SCARLET, appFont = AppFont.ATKINSON, darkTheme = true) {
        AppNavigation(settingsViewModel = viewModel())
    }
}