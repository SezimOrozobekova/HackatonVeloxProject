package com.example.velox.login.compose

import android.app.TimePickerDialog
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.velox.R
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

// Data class for onboarding pages
data class OnboardingPage(val imageRes: Int, val title: String, val description: String)

@OptIn(ExperimentalPagerApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OnboardingScreen(navController: NavController) {
    val staticPages = listOf(
        OnboardingPage(R.drawable.image_lion, "Plan Smarter", "Use AI to manage tasks and reminders effortlessly."),
        OnboardingPage(R.drawable.image_lion, "Track Progress", "Stay motivated by monitoring your achievements."),
        OnboardingPage(R.drawable.image_lion, "Simplify Life", "All-in-one planner designed for your daily success.")
    )

    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableStateOf(0) }
    val totalSteps = staticPages.size + 3
    var name by remember { mutableStateOf("") }
    var wakeHour by remember { mutableStateOf(7) }
    var wakeMinute by remember { mutableStateOf(0) }
    var sleepHour by remember { mutableStateOf(23) }
    var sleepMinute by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (step in 3..5) {
            IconButton(onClick = { if (step > 0) step-- }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            when (step) {
                in 0..2 -> OnboardingPageContent(page = staticPages[step])
                3 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("What’s your name?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Let’s personalize your experience.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Enter your name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                4 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(R.drawable.image_lion), contentDescription = null, modifier = Modifier.height(100.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(buildAnnotatedString {
                            append("$name, what time do you usually\n")
                            withStyle(SpanStyle(color = Color(0xFF0062FF), fontWeight = FontWeight.Bold)) {
                                append("wake up?")
                            }
                        }, textAlign = TextAlign.Center, fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Let’s plan your morning routine and start the day right", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            AndroidView(factory = { context ->
                                NumberPicker(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(200, 300)
                                    minValue = 0
                                    maxValue = 23
                                    value = wakeHour
                                    setOnValueChangedListener { _, _, newVal -> wakeHour = newVal }
                                }
                            })
                            Spacer(modifier = Modifier.width(16.dp))
                            AndroidView(factory = { context ->
                                NumberPicker(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(200, 300)
                                    minValue = 0
                                    maxValue = 59
                                    value = wakeMinute
                                    setOnValueChangedListener { _, _, newVal -> wakeMinute = newVal }
                                }
                            })
                        }
                    }
                }
                5 -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(R.drawable.image_lion), contentDescription = null, modifier = Modifier.height(100.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(buildAnnotatedString {
                            append("What time do you usually\n")
                            withStyle(SpanStyle(color = Color(0xFF0062FF), fontWeight = FontWeight.Bold)) {
                                append("end your day?")
                            }
                        }, textAlign = TextAlign.Center, fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Let’s plan a time to wind down and get ready for sleep", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            AndroidView(factory = { context ->
                                NumberPicker(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(200, 300)
                                    minValue = 0
                                    maxValue = 23
                                    value = sleepHour
                                    setOnValueChangedListener { _, _, newVal -> sleepHour = newVal }
                                }
                            })
                            Spacer(modifier = Modifier.width(16.dp))
                            AndroidView(factory = { context ->
                                NumberPicker(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(200, 300)
                                    minValue = 0
                                    maxValue = 23
                                    value = sleepMinute
                                    setOnValueChangedListener { _, _, newVal -> sleepMinute = newVal }



                                    try {
                                        val numberPickerClass = Class.forName("android.widget.NumberPicker")
                                        val fields = numberPickerClass.declaredFields
                                        for (field in fields) {
                                            if (field.name == "mSelectorWheelPaint") {
                                                field.isAccessible = true
                                                val paint = field.get(this) as android.graphics.Paint
                                                paint.textSize = 200f  // increase size here
                                                invalidate()
                                                break
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }

        // Indicator
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(totalSteps) { index ->
                val isSelected = step == index
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2196F3) else Color.LightGray)
                )
            }
        }

        Button(
            onClick = {
                if (step < totalSteps - 1) step++ else navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (step == totalSteps - 1) "Get Started" else "Next",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.image_lion),
            contentDescription = null,
            modifier = Modifier.height(260.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = page.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}