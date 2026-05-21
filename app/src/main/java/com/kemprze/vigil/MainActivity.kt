package com.kemprze.vigil

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kemprze.vigil.data.DarkModePreferences
import com.kemprze.vigil.model.AddTaskWizard
import com.kemprze.vigil.model.CalendarScreen
import com.kemprze.vigil.model.EditTaskScreen
import com.kemprze.vigil.model.StatsScreen
import com.kemprze.vigil.model.TaskScreen
import com.kemprze.vigil.model.settings.SettingsScreen
import com.kemprze.vigil.model.tasks.TasksViewModel
import com.kemprze.vigil.model.tasks.TasksViewModelFactory
import com.kemprze.vigil.model.settings.SettingsViewModel
import com.kemprze.vigil.navigation.Screen
import com.kemprze.vigil.ui.theme.AppTheme
import com.kemprze.vigil.ui.theme.TODOPrototypingTheme
import com.kemprze.vigil.ui.theme.AppFont

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
                onStatsClick = { navController.navigate(Screen.StatsScreen.route) },
                onEditClick = { task -> navController.navigate(Screen.EditTaskScreen.createRoute(task.id))}
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
                },
                onSuggestCategory = { name -> tasksViewModel.suggestCategory(name) }
            )
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
                onNavigateBack = { navController.navigateUp() },
                tasksViewModel = tasksViewModel
            )
        }
        composable(route = Screen.EditTaskScreen.route,
            arguments = listOf(navArgument("task_id") {
                type = NavType.StringType }
            )) {
            backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("task_id") ?: return@composable
            EditTaskScreen(
                taskId = taskId,
                tasksViewModel = tasksViewModel,
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