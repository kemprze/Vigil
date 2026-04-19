package com.kemprze.todoprototyping.navigation

sealed class Screen(val route: String) {
    object TaskListScreen : Screen("task_list_screen")
    object AddTaskScreen: Screen("add_task_screen")
    object SettingsScreen: Screen("settings_screen")
    object CalendarScreen: Screen("calendar_screen")
}