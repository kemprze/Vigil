package com.kemprze.vigil.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Task
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<Task>,
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val tasksByDay = remember(tasks, currentMonth) {
        tasks.filter { task ->
            val date = task.dueDate
            date != null && date.year == currentMonth.year && date.monthValue == currentMonth.monthValue
        }
            .groupBy { it.dueDate!!.dayOfMonth }
            .mapValues { (_, dayTasks) ->
                dayTasks.map { it.category }
                    .filter { it != Category.NONE }
                    .toSet()
            }
    }

    val selectedDayTasks = remember(tasks, selectedDate) {
        tasks.filter { it.dueDate?.toLocalDate() == selectedDate }
    }

    Scaffold(
        topBar = { TopAppBar(
            title = {
                Text(
                    text = currentMonth.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()) + " ${currentMonth.year}"
                    )
                 },
            navigationIcon = { IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous month"
                    )
                }
                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next month"
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            CalendarGrid(
                yearMonth = currentMonth,
                tasksByDay = tasksByDay,
                selectedDate = selectedDate,
                onDaySelected = { selectedDate = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedDate.dayOfWeek.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                ) + ", ${selectedDate.dayOfMonth} " + selectedDate.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.getDefault()
                ),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (selectedDayTasks.isEmpty()) {
                Text(
                    text = "No tasks this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedDayTasks) { task ->
                        TaskCard(
                            task = task,
                            onTaskCompleted = { _, _ -> },
                            onTaskDeleted = { _ -> },
                            onEditClick = { _ -> },
                            onBreakdownClick = { },
                            subtasks = emptyList(),
                            onSubtaskCompleted = { _, _ -> },
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }
}


    @Composable
    private fun CalendarGrid(
        yearMonth: YearMonth,
        tasksByDay: Map<Int, Set<Category>>,
        selectedDate: LocalDate,
        onDaySelected: (LocalDate) -> Unit
    ) {
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1
        val totalCells = firstDayOffset + daysInMonth

        Column{
            var cellIndex = 0
            while (cellIndex < totalCells) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (cellIndex < firstDayOffset || dayNumber > daysInMonth) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val date = yearMonth.atDay(dayNumber)
                            val categories = tasksByDay[dayNumber] ?: emptySet()

                            DayCell(
                                day = dayNumber,
                                categories = categories,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                onClick = { onDaySelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        cellIndex++
                    }
                }
            }
        }
    }


@Composable
private fun DayCell(
    day: Int,
    categories: Set<Category>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.height(8.dp)
        ) {
            categories.take(3).forEach {
                category ->
                Box(modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        category.color
                    )
                )
            }
        }
    }
}