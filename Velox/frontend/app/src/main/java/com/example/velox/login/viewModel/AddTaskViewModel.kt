package com.example.velox.login.viewModel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.velox.login.compose.ReminderFrequency
import androidx.compose.runtime.State
import com.example.velox.network.ApiClient
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID


// Task data class
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryId: Int,
    val hasReminder: Boolean,
    val location: String,
    val notes: String,
    val frequency: ReminderFrequency,
    val date: String,
    val timeStart: String,
    val timeEnd: String,
    @Transient
    var localDateTime: LocalDateTime
): Serializable


data class Category(
    val id: Int,
    val name: String
)



class AddTaskViewModel(context: Context) : ViewModel() {
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // task data fields
    private val _taskTitle = mutableStateOf("")
    val taskTitle: State<String> = _taskTitle

    private val _date = mutableStateOf<LocalDate?>(null)
    @RequiresApi(Build.VERSION_CODES.O)
    private val _time = mutableStateOf(LocalTime.now())
    val time: String
        @RequiresApi(Build.VERSION_CODES.O)
        get() = _time.value.format(timeFormatter)


    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    private val _selectedCategoryId = mutableStateOf<Int?>(null)
    val selectedCategoryId: State<Int?> = _selectedCategoryId


    private val _taskNotes = mutableStateOf("")
    val taskNotes: State<String> = _taskNotes

    private val _taskLocation = mutableStateOf("")
    val taskLocation: State<String> = _taskLocation

    private val _selectedFrequency = mutableStateOf(ReminderFrequency.NONE)
    val selectedFrequency: State<ReminderFrequency> = _selectedFrequency

    private val _isReminder = mutableStateOf(false)
    val isReminder: State<Boolean> = _isReminder

    // Update functions for each field
    fun updateTaskTitle(newTitle: String) {
        _taskTitle.value = newTitle
    }

    fun updateIsReminder(value: Boolean){
        _isReminder.value = value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLocalTime(hour: Int, minute: Int){
        _time.value = LocalTime.of(hour, minute)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateDate(timeMilis: Long) {
        _date.value = Instant.ofEpochMilli(timeMilis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun isReadyToCreate(): Boolean {
        return _taskTitle.value.isNotEmpty() &&
                _taskLocation.value.isNotEmpty() &&
                _taskNotes.value.isNotEmpty() && _date.value != null
        // Note: Not checking category and frequency as they always have default values
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTask(): Task {
        // Combine date and time to create LocalDateTime
        val localDateTime = LocalDateTime.of(
            _date.value ?: LocalDate.now(),
            _time.value
        )
        val date = _date.value ?: LocalDate.now()
        val dateStr = date.toString() // ISO формат "YYYY-MM-DD"

        //endtime = startTime + 1 // formating time
        val startTime = _time.value
        val endTime = startTime.plusHours(1)
        val timeStartStr = startTime.format(timeFormatter) // "HH:mm"
        val timeEndStr = endTime.format(timeFormatter) // "HH:mm"


        val task = Task(
            title = _taskTitle.value,
            categoryId = _selectedCategoryId.value ?: -1,  // Converting enum to string
            hasReminder = _isReminder.value,
            location = _taskLocation.value,
            notes = _taskNotes.value,
            frequency = _selectedFrequency.value,
            date = dateStr,
            timeStart = timeStartStr,
            timeEnd = timeEndStr,
            localDateTime = localDateTime
        )
        task.localDateTime = LocalDateTime.parse("${task.date}T${task.timeStart}")
        android.util.Log.d("CreateTask", "Task being created: $task")
        return task
    }

    fun updateSelectedCategory(categoryId: Int) {
        _selectedCategoryId.value = categoryId
    }

    fun loadCategories(context: Context) {
        ApiClient.getCategories(context) { categories ->
            if (categories != null) {
                _categories.value = categories
                if (categories.isNotEmpty()) {
                    _selectedCategoryId .value = categories[0].id // выбираем первую по умолчанию
                }
            }
        }
    }


    fun updateTaskNotes(newNotes: String) {
        _taskNotes.value = newNotes
    }

    fun updateTaskLocation(newLocation: String) {
        _taskLocation.value = newLocation
    }

    fun updateSelectedFrequency(newFrequency: ReminderFrequency) {
        _selectedFrequency.value = newFrequency
    }

    // Reset all fields to their default values
    fun resetTaskFields() {
        _taskTitle.value = ""
        _isReminder.value = false
        _taskNotes.value = ""
        _taskLocation.value = ""
        _selectedFrequency.value = ReminderFrequency.NONE
    }



}