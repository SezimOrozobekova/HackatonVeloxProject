package com.example.velox.login.compose

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.velox.login.viewModel.Task
import com.example.velox.network.ApiClient
import java.time.LocalDateTime
import java.util.*

data class CategoryStatistic(
    val category: TaskCategory,
    val count: Int,
    val percentage: Float
)


enum class TaskCategory(val displayName: String, val icon: ImageVector) {
    WORK("Work", Icons.Default.Work),
    STUDY("Study", Icons.Default.School),
    PERSONAL("Personal", Icons.Default.Person),
    SHOPPING("Shopping", Icons.Default.ShoppingCart),
    HEALTH("Health", Icons.Default.Favorite),
    HOME("Home", Icons.Default.Home),
    OTHER("Other", Icons.Default.MoreHoriz)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    onBackClicked: () -> Unit,
) {
    val context = LocalContext.current
    val taskList = remember { mutableStateListOf<Task>() }
    var isLoading by remember { mutableStateOf(true) }
    val categoryMap = remember { mutableStateMapOf<Int, TaskCategory>() }

    LaunchedEffect(Unit) {
        // loading all categories
        ApiClient.getCategories(context) { categories ->
            categories?.forEach { category ->
                // convert categories into work
                val taskCategory = TaskCategory.entries.find { it.name == category.name.uppercase() }
                if (taskCategory != null) {
                    categoryMap[category.id] = taskCategory
                }
            }

            // uploading all tasks
            ApiClient.getTasks(context) { tasks ->
                if (tasks != null) {
                    taskList.clear()
                    taskList.addAll(tasks)
                }
                isLoading = false
            }
        }
    }


    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val statistics = remember(taskList, categoryMap) {
        calculateStatistics(taskList, categoryMap)
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar with Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Task Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                OverviewSection(
                    totalTasks = taskList.size,
                    tasksWithReminders = taskList.count { it.hasReminder },
                    upcomingTasks = taskList.count { it.localDateTime.isAfter(LocalDateTime.now()) }
                )
            }

            item {
                CategoryDistributionSection(statistics.categoryStats)
            }

            item {
                FrequencyAnalysisSection(taskList)
            }

            item {
                UpcomingTasksSection(
                    upcomingTasks = taskList.filter { it.localDateTime.isAfter(LocalDateTime.now()) }
                        .sortedBy { it.localDateTime }
                        .take(5),
                    categoryMap = categoryMap
                )

            }
        }
    }
}


@Composable
fun OverviewSection(
    totalTasks: Int,
    tasksWithReminders: Int,
    upcomingTasks: Int
) {
    Text(
        text = "Overview",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                title = "Total Tasks",
                value = totalTasks.toString(),
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            StatCard(
                title = "With Reminders",
                value = tasksWithReminders.toString(),
                icon = Icons.Default.NotificationsActive,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            StatCard(
                title = "Upcoming",
                value = upcomingTasks.toString(),
                icon = Icons.Default.Schedule,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryDistributionSection(categoryStats: List<CategoryStatistic>) {
    Text(
        text = "Category Distribution",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Pie Chart
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                PieChart(categoryStats)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legend
            categoryStats.forEach { stat ->
                CategoryLegendItem(stat)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PieChart(data: List<CategoryStatistic>) {
    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        var startAngle = 0f

        data.forEach { stat ->
            val sweepAngle = stat.percentage * 360f
            drawArc(
                color = getCategoryColor(stat.category),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryLegendItem(stat: CategoryStatistic) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(getCategoryColor(stat.category))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = stat.category.icon,
            contentDescription = stat.category.displayName,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stat.category.displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${stat.count} (${(stat.percentage * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FrequencyAnalysisSection(tasks: List<Task>) {
    val frequencyData = tasks.groupBy { it.frequency }
        .mapValues { it.value.size }

    Text(
        text = "Task Frequency",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ReminderFrequency.values().forEach { frequency ->
                val count = frequencyData[frequency] ?: 0
                FrequencyItem(
                    frequency = frequency.name,
                    count = count,
                    total = tasks.size
                )
                if (frequency != ReminderFrequency.values().last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun FrequencyItem(
    frequency: String,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) count.toFloat() / total else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = frequency.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun UpcomingTasksSection(upcomingTasks: List<Task>, categoryMap: Map<Int, TaskCategory>){
    Text(
        text = "Upcoming Tasks",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (upcomingTasks.isEmpty()) {
                Text(
                    text = "No upcoming tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                upcomingTasks.forEachIndexed { index, task ->
                    val category = categoryMap[task.categoryId] ?: TaskCategory.OTHER
                    UpcomingTaskItem(task, category)
                    if (index < upcomingTasks.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun UpcomingTaskItem(task: Task, category: TaskCategory)
 {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.displayName,
            tint = getCategoryColor(category),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${task.date} • ${task.timeStart}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (task.hasReminder) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Has reminder",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Helper function to calculate statistics
data class TaskStatistics(
    val categoryStats: List<CategoryStatistic>
)

fun calculateStatistics(
    tasks: List<Task>,
    categoryMap: Map<Int, TaskCategory>
): TaskStatistics {
    val mappedCategories = tasks.mapNotNull { task ->
        categoryMap[task.categoryId]
    }

    val categoryGroups = mappedCategories.groupingBy { it }.eachCount()
    val totalTasks = tasks.size

    val categoryStats = categoryGroups.map { (category, count) ->
        val percentage = if (totalTasks > 0) count.toFloat() / totalTasks else 0f
        CategoryStatistic(category, count, percentage)
    }

    return TaskStatistics(categoryStats)
}


internal fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.WORK -> Color(0xFF5D9CEC)
        TaskCategory.STUDY -> Color(0xFFAC92EC)
        TaskCategory.PERSONAL -> Color(0xFF4FC1E9)
        TaskCategory.SHOPPING -> Color(0xFFFFCE54)
        TaskCategory.HEALTH -> Color(0xFFED5565)
        TaskCategory.HOME -> Color(0xFF48CFAD)
        TaskCategory.OTHER -> Color(0xFFCCD1D9)
    }
}