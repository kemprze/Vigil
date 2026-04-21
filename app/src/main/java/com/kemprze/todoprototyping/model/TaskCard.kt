package com.kemprze.todoprototyping.model

import android.R
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kemprze.todoprototyping.data.model.Category
import com.kemprze.todoprototyping.data.model.Duration
import com.kemprze.todoprototyping.data.model.Priority
import com.kemprze.todoprototyping.data.model.simpleTask
import com.kemprze.todoprototyping.ui.theme.TODOPrototypingTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DetailsRow(dueDate: LocalDateTime?,
               priority: Priority,
               category: Category,
               duration: Duration,
               onEditClick: () -> Unit,
               modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    if (dueDate != null) {
        val formattedDataString = dueDate.format(formatter)
    }

    Column(modifier = Modifier
        .padding(8.dp)
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        if (category != Category.NONE) Text(
            text = stringResource(category.categoryImageRes) + " " +
                    stringResource(category.categoryNameRes),
            style = MaterialTheme.typography.bodyMedium,
            color = category.color,
            modifier = modifier.padding(top = 4.dp)
        )

        Text(
            text = "${priority.emoji} ${priority.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (priority.level == 1)
                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)) {
            Icon(
                Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = duration.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (dueDate != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dueDate.format(formatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit task"
                )
            }
        }
    }
}

@Composable
fun TaskCard(task: simpleTask,
             onTaskCompleted: (simpleTask, Boolean) -> Unit,
             onTaskDeleted: (simpleTask) -> Unit,
             onEditClick: (simpleTask) -> Unit,
             modifier: Modifier = Modifier) {
    var details by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.75f },
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onTaskDeleted(task)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }

        }
    ) {
        Card(onClick = { details = !details }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                TaskNameDescription(
                    name = task.taskName,
                    description = task.taskDescription,
                    category = task.category,
                    modifier = Modifier.weight(6f)
                )
                TaskCheckbox(
                    modifier = Modifier.weight(1f),
                    priority = task.priority,
                    checked = task.isCompleted,
                    onCheckedChange = { isChecked -> onTaskCompleted(task, isChecked) })
            }
            if (details) DetailsRow(
                dueDate = task.dueDate,
                category = task.category,
                duration = task.duration,
                priority = task.priority,
                onEditClick = { onEditClick(task) }
            )
        }
    }
}

@Composable
fun TaskNameDescription(name: String, description: String, category: Category, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row() {
            Text(
                text = stringResource(category.categoryImageRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun TaskCheckbox(modifier: Modifier = Modifier,
                 priority: Priority,
                 checked: Boolean,
                 onCheckedChange: (Boolean) -> Unit
    )
    {
    val importantColorSchemeChange = if (priority.level == 1) {
        CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.error,
            uncheckedColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6F)
        )
    }
     else {
         CheckboxDefaults.colors()
     }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Checkbox(
            checked = checked,
            colors = importantColorSchemeChange,
            onCheckedChange = onCheckedChange
            )
    }
}

@Preview
@Composable
fun TaskCardPreview() {
    val sampleTask = simpleTask(
        taskName = "Throw trash out",
        taskDescription = "The trashcan is overflowing",
        dueDate = LocalDateTime.of(2025, 12, 1, 12, 42),
        category = Category.HOME,
        createdOn = LocalDateTime.of(2024, 10, 12, 12, 47),
        priority = Priority.NORMAL,
        isCompleted = false
    )
    TODOPrototypingTheme {
        TaskCard(
                sampleTask,
        onTaskCompleted = {_, _ -> },
            onTaskDeleted = { },
            onEditClick = { }
        )
    }
}

@Preview()
@Composable
fun TaskCardPreviewDark() {
    val sampleTask = simpleTask(
        taskName = "Throw trash out",
        taskDescription = "The trashcan is overflowing",
        dueDate = LocalDateTime.of(2025, 12, 1, 12, 42),
        category = Category.HOME,
        createdOn = LocalDateTime.of(2024, 10, 12, 12, 47),
        priority = Priority.NORMAL,
        isCompleted = false
    )
    TODOPrototypingTheme(darkTheme = true) {
        TaskCard(
            sampleTask,
            onTaskCompleted = {_, _ -> },
            onTaskDeleted = {},
            onEditClick = { }
        )
    }
    }
