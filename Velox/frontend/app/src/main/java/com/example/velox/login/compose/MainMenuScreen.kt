import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.velox.login.compose.*
import com.example.velox.login.viewModel.Task
import com.example.velox.MainRecordAudio
import com.example.velox.VoiceAssistant
import com.example.velox.login.LoginComposeActivity
import com.example.velox.login.compose.WeeklyCalendarScreen
import com.example.velox.settings.ProfileSettingsScreen
import com.example.velox.speakTodayTasks



@RequiresApi(Build.VERSION_CODES.O)
fun speakTodayTasks(context: Context) {
    VoiceAssistant.speakTodayTasks(context)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainMenuScreen(
    modifier: Modifier = Modifier,
    taskList: List<Task>,
    onAddClicked: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        VoiceAssistant.init(context)
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {

                    // 🔷 Gradient Button — запись голосовой задачи
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF7C4DFF), Color(0xFFFF4081))
                                )
                            )
                            .clickable {
                                val intent = Intent(context, MainRecordAudio::class.java)
                                context.startActivity(intent)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Gradient Action",
                            tint = Color.White
                        )
                    }

                    // 🔊 Новая кнопка — озвучить задачи на сегодня
                    Button(
                        onClick = { speakTodayTasks(context) },
                        modifier = Modifier
                            .padding(top = 10.dp, end = 0.dp)
                    ) {
                        Text(text = "🔊 Today")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔵 Main blue FAB — добавить задачу вручную
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                            .clickable { onAddClicked() }
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable("home") {
                ScheduleScreen()
            }
            composable("calendar") {
                WeeklyCalendarScreen(navController = navController, context = context)
            }
            composable("profile") {
                ProfileSettingsScreen(
                    onIntegrateClick = {},
                    onNotificationsClick = {},
                    onStatisticsClick = {
                        navController.navigate("statisticsScreen")
                    },
                    onCategoryClick = {
                        navController.navigate("categoryScreen")
                    },
                    onLogoutClick = {
                        val sharedPref = context.getSharedPreferences("velox_prefs", android.content.Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            remove("access_token")
                            remove("refresh_token")
                            apply()
                        }

                        val intent = Intent(context, LoginComposeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                )
            }
            composable("statisticsScreen") {
                StatisticsScreen(onBackClicked = navController::popBackStack)
            }
            composable("categoryScreen") {
                CategoryScreen(onBackClicked = navController::popBackStack)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen("home", "Home", Icons.Default.Home),
        Screen("calendar", "Calendar", Icons.Default.DateRange),
        Screen("profile", "Profile", Icons.Default.Person)
    )

    var currentRoute by remember { mutableStateOf(items[0].route) }

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors().copy(
                    selectedIconColor = Color(0xFFFF5722),
                    selectedIndicatorColor = Color.Transparent,
                ),
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route)
                        currentRoute = screen.route
                    }
                }
            )
        }
    }
}

data class Screen(val route: String, val label: String, val icon: ImageVector)
