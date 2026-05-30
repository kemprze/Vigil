package com.kemprze.vigil.model

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kemprze.vigil.R
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Duration
import com.kemprze.vigil.data.model.Priority
import com.kemprze.vigil.data.model.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DetailsRow(dueDate: LocalDateTime?,
               priority: Priority,
               category: Category,
               duration: Duration,
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
    }
}

@Composable
fun TaskCard(task: Task,
             subtasks: List<Task> = emptyList(),
             onTaskCompleted: (Task, Boolean) -> Unit,
             onSubtaskCompleted: (Task, Boolean) -> Unit = { _, _ -> },
             onTaskDeleted: (Task) -> Unit,
             onEditClick: (Task) -> Unit,
             onBreakdownClick: (Task) -> Unit,
             isAiModelReady: Boolean,
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
                    contentDescription = stringResource(R.string.cd_delete_task),
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
                priority = task.priority
            )

            if (details) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp, vertical = 4.dp
                        )
                ) {
                    val dotColor = MaterialTheme.colorScheme.primary
                    subtasks.forEach {
                        subtask ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                        .clickable {
                                            onSubtaskCompleted(
                                                subtask,
                                                !subtask.isCompleted
                                            )
                                        }
                                ) {
                                    Canvas(
                                        modifier = Modifier.size(8.dp)
                                    ) {
                                        if (subtask.isCompleted)
                                            drawCircle(
                                                color = dotColor,
                                                radius = size.minDimension / 2
                                            )
                                        else
                                            drawCircle(
                                                color = dotColor,
                                                radius = size.minDimension / 2,
                                                style = Stroke(
                                                    width = 2.dp.toPx()
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = subtask.taskName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (subtask.isCompleted) MaterialTheme.colorScheme.outline
                                                else MaterialTheme.colorScheme.onSurface
                                        )
                                }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            onClick = { onEditClick(task) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                                .padding(start = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.cd_edit_task),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (isAiModelReady) {
                            Surface(
                                onClick = { onBreakdownClick(task) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                                    .padding(start = 4.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = stringResource(R.string.cd_breakdown_task),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
