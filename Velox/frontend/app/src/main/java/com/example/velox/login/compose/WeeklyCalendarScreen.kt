package com.example.velox.login.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.velox.login.viewModel.Task
import com.example.velox.network.ApiClient
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*
import kotlin.random.Random

data class CalendarEvent(
    val title: String,
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int,
    val color: Color
)

fun generatePastelColor(): Color {
    val rainbowHues = listOf(
        0f,
        30f,
        60f,
        120f,
        180f,
        240f,
        270f
    )

    val hue = rainbowHues.random()
    val saturation = 0.4f
    val lightness = 0.85f

    return Color.hsl(hue, saturation, lightness)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyCalendarScreen(
    navController: NavController,
    context: Context
) {
    val hours = (6..23).toList()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var categoryColors by remember { mutableStateOf<Map<Int, Color>>(emptyMap()) }

    LaunchedEffect(Unit) {
        ApiClient.getTasks(context) { loadedTasks ->
            if (loadedTasks != null) {
                tasks = loadedTasks
                val uniqueCategoryIds = loadedTasks.map { it.categoryId }.toSet()
                categoryColors = uniqueCategoryIds.associateWith { generatePastelColor() }
            }
        }
    }

    val weekFields = WeekFields.of(Locale.getDefault())
    val firstDayOfWeek = selectedDate.with(weekFields.dayOfWeek(), 1)
    val days = (0..6).map { firstDayOfWeek.plusDays(it.toLong()) }

    val events = remember(tasks, selectedDate) {
        tasks.filter { task ->
            val taskDate = LocalDate.parse(task.date)
            taskDate in days.first()..days.last()
        }.map { task ->
            val start = LocalDateTime.parse("${task.date}T${task.timeStart}")
            val end = LocalDateTime.parse("${task.date}T${task.timeEnd}")
            CalendarEvent(
                title = task.title,
                dayOfWeek = start.dayOfWeek.value % 7,
                startHour = start.hour,
                endHour = end.hour,
                color = categoryColors[task.categoryId] ?: Color.LightGray
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { selectedDate = selectedDate.minusWeeks(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Weekly Calendar",
                    color = Color(0xFF0062FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = { selectedDate = selectedDate.plusWeeks(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Week")
            }
        }

        // Calendar Grid
        Box(modifier = Modifier.fillMaxSize()) {
            // Grid background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dayWidth = size.width / 7
                val hourHeight = size.height / (hours.size + 1)

                // Vertical lines (day separators)
                for (i in 1..6) {
                    val x = dayWidth * i
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.5f
                    )
                }

                // Horizontal lines (hour separators)
                for (i in 0..hours.size) {
                    val y = hourHeight * i
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5f
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Days header
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Empty cell for time column
                    Spacer(modifier = Modifier.width(60.dp))

                    days.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Hours and content
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(hours) { hour ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Time column
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = String.format("%02d:00", hour),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Day columns
                            days.forEachIndexed { dayIndex, _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .padding(horizontal = 1.dp, vertical = 0.5.dp)
                                ) {
                                    val hourEvents = events.filter { event ->
                                        event.dayOfWeek == dayIndex &&
                                                event.startHour <= hour &&
                                                event.endHour > hour
                                    }

                                    if (hourEvents.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            hourEvents.forEach { event ->
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .background(
                                                            event.color.copy(alpha = 0.7f),
                                                            shape = MaterialTheme.shapes.small
                                                        ),
                                                    contentAlignment = Alignment.TopStart
                                                ) {
                                                    Text(
                                                        text = event.title,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(2.dp),
                                                        maxLines = 2,
                                                        lineHeight = 10.sp
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
            }
        }
    }
}