package com.kemprze.vigil.model

import android.R.color.transparent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.kemprze.vigil.R
import com.kemprze.vigil.data.model.Task
import com.kemprze.vigil.model.tasks.TasksViewModel
import com.kemprze.vigil.ui.theme.themePrimaryColor
import java.time.LocalTime
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    tasksViewModel: TasksViewModel,
    onNavigateBack: () -> Unit
) {
    val taskUiState = tasksViewModel.uiState.collectAsState()
    val scheduledTasks = taskUiState.value.tasks.filter { it.dueDate != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.timeline_view_button
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            TimelineGrid(
                tasks = scheduledTasks
            )
        }
    }
}

@Composable
fun TimelineGrid(
    tasks: List<Task>,
    modifier: Modifier = Modifier
) {
    val hourHeight = 64.dp
    val startHour = 7
    val endHour = 22
    val hourCount = endHour - startHour

    val hourLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()
    val now = LocalTime.now()
    val nowY = ((now.hour - startHour) * 60 + now.minute) / 60f * hourHeight
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxHeight()
            .height(hourHeight * (hourCount + 1))
            .padding(top = 12.dp)
    ) {


        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            for (i in 0..hourCount) {
                val y = i * hourHeight.toPx()
                val hour = startHour + i
                val label = when {
                    hour < 12 -> "${hour}AM"
                    hour == 12 -> "12PM"
                    else -> "${hour - 12}PM"
                }

                drawLine(
                    color = gridLineColor,
                    start = Offset(60.dp.toPx(), y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )

                val textLayout = textMeasurer.measure(
                    label,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        color = hourLabelColor
                    )
                )

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = 4.dp.toPx(),
                        y = y - textLayout.size.height / 2f
                    )
                )
            }

            drawLine(
                color = primaryColor,
                start = Offset(60.dp.toPx(), nowY.toPx()),
                end = Offset(size.width, nowY.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            drawCircle(
                color = primaryColor,
                radius = 5.dp.toPx(),
                center = Offset(60.dp.toPx(), nowY.toPx())
            )
        }

        tasks.forEach {
            task ->
            TimelineTaskBlock(
                task = task,
                hourHeightDp = hourHeight,
                topPadding = 12.dp,
                startHour = startHour

            )
        }
    }
}

@Composable
fun TimelineTaskBlock(
    task: Task,
    hourHeightDp: Dp,
    topPadding: Dp,
    startHour: Int,
    modifier: Modifier = Modifier
) {
    val dueDateHour = task.dueDate!!.hour
    val dueDateMinute = task.dueDate!!.minute
    val minutesFromStart = (dueDateHour - startHour) * 60 + dueDateMinute
    val blockHeight = (task.duration.maxMinutes.coerceAtMost(120) / 60f) * hourHeightDp
    val yOffset = (minutesFromStart / 60f) * hourHeightDp + topPadding
    val surfaceColor = MaterialTheme.colorScheme.surface
    val categoryColor = task.category.color

    Box(
        modifier = Modifier
            .offset(x = 64.dp, y = yOffset)
            .fillMaxWidth()
            .padding(end = 8.dp)
            .height(blockHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceColor.copy(alpha = 0.2f))
            .background(Brush.horizontalGradient(listOf(categoryColor.copy(alpha = 0.4f), Color.Transparent)))
            .border(border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(categoryColor.copy(alpha = 0.4f), Color.Transparent))))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.15f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = stringResource(task.category.categoryImageRes),
                    fontSize = 16.sp
                )
            }
            Column(
                modifier = Modifier.weight(0.85f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.taskName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )

                if (!task.taskDescription.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.taskDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}