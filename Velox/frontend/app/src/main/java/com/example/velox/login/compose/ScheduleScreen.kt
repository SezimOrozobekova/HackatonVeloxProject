package com.example.velox.login.compose

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.velox.VoiceAssistant
import com.example.velox.login.viewModel.AddTaskViewModel
import com.example.velox.login.viewModel.Task
import com.example.velox.network.ApiClient
import com.example.velox.network.ApiClient.deleteTaskById
import io.github.chouaibmo.rowkalendar.RowKalendar
import io.github.chouaibmo.rowkalendar.components.DateCellDefaults
import io.github.chouaibmo.rowkalendar.extensions.isBefore
import io.github.chouaibmo.rowkalendar.extensions.now
import kotlinx.datetime.LocalDate as KtLocalDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier
) {
    var chosenDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current
    var dbTaskList by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // additing option to edit
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    val addTaskViewModel = remember { AddTaskViewModel(context) }

    fun loadTasks() {
        isLoading = true
        error = null
        ApiClient.getTasks(context) { tasks ->
            isLoading = false
            tasks?.let {
                dbTaskList = it
                Log.d("ScheduleScreen", "Loaded ${it.size} tasks")
            } ?: run {
                error = "Failed to load tasks"
                Log.e("ScheduleScreen", "Error loading tasks")
            }
        }
    }

    // Load all tasks once
    LaunchedEffect(Unit) {
        loadTasks()
        VoiceAssistant.init(context)
    }

    // show edit, if taskToEdit не null
    taskToEdit?.let { task ->
        EditTaskScreen(
            viewModel = addTaskViewModel,
            task = task,
            onTaskUpdateClicked = { updatedTask ->
                // Обновляем задачу в списке
                dbTaskList = dbTaskList.map { if (it.id == updatedTask.id) updatedTask else it }
                taskToEdit = null // Закрываем экран редактирования
                Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
            },
            onCancelClicked = {
                taskToEdit = null // Закрываем экран редактирования без изменений
            }
        )
        return
    }


    // Filter tasks by chosenDate
    val filteredTasks by remember(chosenDate, dbTaskList) {
        derivedStateOf {
            dbTaskList.filter { task ->
                try {
                    task.localDateTime.toLocalDate() == chosenDate
                } catch (e: Exception) {
                    Log.e("ScheduleScreen", "Error parsing date for task ${task.id}", e)
                    false
                }
            }.sortedBy { it.localDateTime }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF9F9))
    ) {
        ScheduleHeader(
            date = chosenDate,
            modifier = Modifier.fillMaxWidth()
        )

        // 🔊 Кнопка озвучивания задач для выбранной даты
        Button(
            onClick = {
                VoiceAssistant.speakTasksForDate(context, chosenDate)
            },
            modifier = Modifier
                .padding(start = 16.dp, bottom = 8.dp)
        ) {
            Text(text = "🔊 Озвучить задачи")
        }


        // calendar
        DateSelector(
            initialDate = chosenDate,
            onDateSelected = { newDate -> chosenDate = newDate }
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error!!, color = Color.Red)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (filteredTasks.isEmpty()) {
                        item {
                            EmptyTaskView()
                        }
                    } else {
                        items(items = filteredTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                onClick = { /* handle click */ },
                                onEdit = {
                                    taskToEdit = task // Открываем экран редактирования для этой задачи
                                },
                                onDelete = {
                                    deleteTaskById(context, task.id) { success ->
                                        if (success) {
                                            loadTasks() // перезагрузка после удаления
                                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                            Divider(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


// component with local chosen date
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    RowKalendar(
        modifier = Modifier.fillMaxWidth(),
        content = { date, isSelected, onClick ->
            CustomDateCell(
                date = date,
                isSelected = isSelected,
                shape = RoundedCornerShape(12.dp),
                elevation = DateCellDefaults.DateCellElevation(
                    selectedElevation = 4.dp,
                    pastElevation = 2.dp,
                    futureElevation = 2.dp
                ),
                border = DateCellDefaults.border(
                    selectedBorderColor = Color.Transparent,
                    pastBorderColor = Color.Transparent,
                    futureBorderColor = Color.Transparent,
                ),
                colors = DateCellDefaults.colors(
                    selectedContainerColor = Color(0xFFFF7648),
                    selectedTextColor = Color.White,
                    pastContainerColor = Color.White,
                    pastTextColor = Color.Black,
                    futureContainerColor = Color.White,
                    futureTextColor = Color.Black
                ),
                modifier = Modifier
                    .height(60.dp)
                    .width(50.dp),
                onDateSelected = {
                    selectedDate = LocalDate.of(it.year, it.monthNumber, it.dayOfMonth)
                    onClick(it)
                    onDateSelected(selectedDate)
                },
            )
        }
    )
}


@Composable
fun CustomDateCell(
    modifier: Modifier = Modifier,
    date: kotlinx.datetime.LocalDate,
    isSelected: Boolean = false,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit,
    shape: Shape = DateCellDefaults.shape,
    colors: DateCellDefaults.DateCellColors = DateCellDefaults.colors(),
    elevation: DateCellDefaults.DateCellElevation = DateCellDefaults.elevation(),
    border: DateCellDefaults.DateCellBorder? = null
) {

    val cellColor = when {
        isSelected -> colors.selectedContainerColor
        date.isBefore(kotlinx.datetime.LocalDate.now()) -> colors.pastContainerColor
        else -> colors.futureContainerColor
    }

    val textColor = when {
        isSelected -> colors.selectedTextColor
        date.isBefore(kotlinx.datetime.LocalDate.now()) -> colors.pastTextColor
        else -> colors.futureTextColor
    }

    val cellBorder = border?.let {
        when {
            isSelected -> BorderStroke(it.selectedBorderWidth, it.selectedBorderColor)
            date.isBefore(kotlinx.datetime.LocalDate.now()) -> BorderStroke(it.pastBorderWidth, it.pastBorderColor)
            else -> BorderStroke(it.futureBorderWidth, it.futureBorderColor)
        }
    }

    val cellElevation = when {
        isSelected -> elevation.selectedElevation
        date.isBefore(kotlinx.datetime.LocalDate.now()) -> elevation.pastElevation
        else -> elevation.futureElevation
    }

    Card(
        modifier = modifier
            .shadow(elevation = cellElevation, shape = shape)
            .clip(shape = shape)
            .clickable { onDateSelected(date) },
        border = cellBorder,
        colors = CardDefaults.cardColors(containerColor = cellColor),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = date.dayOfWeek.name.first().uppercase(),
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun EmptyTaskView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tasks for this day",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = formatTime(task.localDateTime), fontSize = 12.sp)

                if (task.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = task.location, fontSize = 12.sp)
                }

                if (task.frequency != ReminderFrequency.NONE) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = task.frequency.displayName, fontSize = 12.sp)
                }
            }
        }

        // Меню с тремя точками
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                    text = { Text("Edit") }
                )
                DropdownMenuItem(
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                    text = { Text("Delete") }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleHeader(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 32.dp, horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date display
        Box(
            modifier = Modifier
        ) {
            Row {
                // Date number
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Day and month
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.Bottom)
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        ),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${
                            date.month.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            )
                        } ${date.year}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Today button
        if(date.isEqual(LocalDate.now())){
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE6F7EF))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Today",
                    color = Color(0xFF4CD080),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


// Helper functions
@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(dateTime: LocalDateTime): String {
    return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
}
