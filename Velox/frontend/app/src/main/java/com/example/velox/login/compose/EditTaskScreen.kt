package com.example.velox.login.compose

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.velox.login.viewModel.AddTaskViewModel
import com.example.velox.login.viewModel.Task
import com.example.velox.network.ApiClient
import androidx.compose.ui.platform.LocalContext


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    modifier: Modifier = Modifier,
    viewModel: AddTaskViewModel,
    task: Task,
    onTaskUpdateClicked: (Task) -> Unit,
    onCancelClicked: () -> Unit,
) {
    val frequencies = ReminderFrequency.entries
    val context = LocalContext.current

    // Initialize viewModel with task data
    LaunchedEffect(task) {
        viewModel.updateTaskTitle(task.title)
        viewModel.updateTaskLocation(task.location)
        viewModel.updateTaskNotes(task.notes)
        viewModel.updateSelectedFrequency(task.frequency)
        viewModel.updateIsReminder(task.hasReminder)
        viewModel.updateSelectedCategory(task.categoryId)

        // Parse and set date - using kotlinx.datetime
        val date = kotlinx.datetime.LocalDate.parse(task.date)
        val millis = date.toEpochDays() * 86400000L // Convert days to milliseconds
        viewModel.updateDate(millis)

        // Parse and set time - using kotlinx.datetime
        val time = kotlinx.datetime.LocalTime.parse(task.timeStart)
        viewModel.updateLocalTime(time.hour, time.minute)

        // Load categories if not already loaded
        if (viewModel.categories.value.isEmpty()) {
            viewModel.loadCategories(context)
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            AnimatedVisibility(viewModel.isReadyToCreate()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3))
                        .clickable {
                            val updatedTask = viewModel.createTask().copy(id = task.id)
                            Log.d("EditTask", "Sending task: $updatedTask")
                            ApiClient.editTask(context, updatedTask) { success ->
                                if (success) {
                                    onTaskUpdateClicked(updatedTask)
                                }
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save task",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 64.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit task",
                    color = Color(0xFF2196F3),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Cancel",
                    color = Color(0xFFFF5722),
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onCancelClicked() }
                )
            }

            // Task title
            OutlinedTextField(
                value = viewModel.taskTitle.value,
                onValueChange = viewModel::updateTaskTitle,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                placeholder = { Text("Enter task title") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD))
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
            ) {
                Text(
                    text = viewModel.categories.value.firstOrNull { it.id == viewModel.selectedCategoryId.value }?.name
                        ?: "Select category"
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select category"
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.categories.value.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.updateSelectedCategory(category.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState()
            val selectedDate = datePickerState.selectedDateMillis?.let {
                viewModel.updateDate(it)
                convertMillisToDate(it)
            } ?: "Set date"

            Text(
                text = "Date",
                color = Color(0xFF2196F3),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { showDatePicker = !showDatePicker },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = selectedDate,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = selectedDate,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }
                if (showDatePicker) {
                    Popup(
                        onDismissRequest = { showDatePicker = false },
                        alignment = Alignment.TopStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 64.dp)
                                .shadow(elevation = 4.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            DatePicker(
                                state = datePickerState,
                                showModeToggle = false
                            )
                        }
                    }
                }
            }

            // Time
            var showTimePicker by remember { mutableStateOf(false) }

            if (showTimePicker) {
                TimePickerDialog(
                    onDismiss = { showTimePicker = false },
                    onConfirm = {
                        viewModel.updateLocalTime(it.hour, it.minute)
                        showTimePicker = false
                    }
                )
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable {
                        showTimePicker = !showTimePicker
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = viewModel.time,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder
            Text(
                text = "Reminder",
                color = Color(0xFF2196F3),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Set reminder",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )

                Switch(
                    checked = viewModel.isReminder.value,
                    onCheckedChange = viewModel::updateIsReminder,
                    colors = SwitchDefaults.colors().copy(
                        uncheckedTrackColor = Color(0xFFE3F2FD),
                        checkedTrackColor = Color.Green
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            Text(
                text = "Location",
                color = Color(0xFF2196F3),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.taskLocation.value,
                onValueChange = viewModel::updateTaskLocation,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                placeholder = { Text("Enter location") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            Text(
                text = "Notes",
                color = Color(0xFF2196F3),
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = viewModel.taskNotes.value,
                onValueChange = viewModel::updateTaskNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD)),
                placeholder = { Text("Add notes") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequency",
                    color = Color(0xFF2196F3),
                    fontSize = 18.sp
                )
            }

            // Frequency options
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = frequencies, key = { it.ordinal }) { frequency ->
                    FrequencyChip(
                        frequency = frequency,
                        isSelected = frequency == viewModel.selectedFrequency.value,
                        onSelect = { viewModel.updateSelectedFrequency(frequency) }
                    )
                }
            }
        }
    }
}