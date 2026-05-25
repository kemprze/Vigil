package com.kemprze.vigil.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.kemprze.vigil.data.model.Category
import com.kemprze.vigil.data.model.Task
import com.kemprze.vigil.model.tasks.TasksViewModel


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StatsScreen(
    tasksViewModel: TasksViewModel,
    incompleteTasks: List<Task>,
    completedTasks: List<Task>,
    onNavigateBack: () -> Unit
) {
    var showCompleted by remember { mutableStateOf(false) }
    val activeTasks = if (showCompleted) completedTasks else incompleteTasks
    val insight by tasksViewModel.insight.collectAsState()
    val isGeneratingInsight by tasksViewModel.isGeneratingInsight.collectAsState()

    val categoryCounts = remember(activeTasks) {
        Category.entries.map { category ->
            category to activeTasks.count { it.category == category }
        }.filter { (_, count) -> count > 0 }
    }

    val total = remember(activeTasks) { activeTasks.size }
    val aiModelReady by tasksViewModel.aiModelReadyFlow.collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleChoiceSegmentedButtonRow() {
                SegmentedButton(
                    selected = !showCompleted,
                    onClick = { showCompleted = false },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    icon = { },
                    label = { Text("Incomplete") }
                )
                SegmentedButton(
                    selected = showCompleted,
                    onClick = { showCompleted = true },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    icon = { },
                    label = { Text("Complete") }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            DonutChart(
                categoryCounts = categoryCounts,
                total = total,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            categoryCounts.forEach { (category, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(category.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name.lowercase().replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$count (${(count.toFloat() / total * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (aiModelReady) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    LaunchedEffect(Unit) {
                        tasksViewModel.generateInsight(incompleteTasks, completedTasks)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Your insights",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isGeneratingInsight) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                text = insight,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    LaunchedEffect(Unit) {
                        tasksViewModel.generateInsight(incompleteTasks, completedTasks)
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                    Text(
                        text = "Enable AI in Settings to see insights.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )

                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    categoryCounts: List<Pair<Category, Int>>,
    total: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val centerTextColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier.size(240.dp)
    ) {
        if (total == 0) {
            drawArc(
                color = androidx.compose.ui.graphics.Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                size = Size(size.width * 0.8f, size.height * 0.8f),
                style = Stroke(width = 48.dp.toPx())
            )
        } else {
            var startAngle = -90f
            categoryCounts.forEach { (category, count) ->
                val sweep = (count.toFloat() / total.toFloat()) * 360f
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                    size = Size(size.width * 0.8f, size.height * 0.8f),
                    style = Stroke(width = 48.dp.toPx())
                )
                startAngle += sweep
            }
        }

        val label = if (total == 0) "No tasks" else "${total} tasks"
        val textLayout = textMeasurer.measure(
            label, style = TextStyle(fontSize = 18.sp, color = centerTextColor)
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = center.x - textLayout.size.width / 2f,
                y = center.y - textLayout.size.height / 2f
            )
        )
    }
}