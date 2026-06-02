package com.kemprze.vigil.navigation

sealed class Screen(val route: String) {
    object TaskListScreen : Screen("task_list_screen")
    object AddTaskScreen: Screen("add_task_screen")
    object SettingsScreen: Screen("settings_screen")
    object CalendarScreen: Screen("calendar_screen")
    object StatsScreen: Screen("stats_screen")
    object EditTaskScreen: Screen("edit_task_screen/{task_id}") {
        fun createRoute(task_id: String) = "edit_task_screen/${task_id}"
    }
    object TimelineScreen: Screen("timeline_screen")
}