package com.example.velox.login

import MainMenuScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.velox.login.compose.WeeklyCalendarScreen
import com.example.velox.login.compose.*
import com.example.velox.login.viewModel.AddTaskViewModel
import com.example.velox.login.viewModel.Task

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext


class LoginComposeActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addTaskViewModel = AddTaskViewModel(this)
        setContent {
            MaterialTheme {
                AppNavigation(addTaskViewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(addTaskViewModel: AddTaskViewModel) {
    val navController = rememberNavController()
    var taskList by remember { mutableStateOf(listOf<Task>()) }

    NavHost(navController = navController, startDestination = "authCheckScreen") {

        composable("authCheckScreen") {
            AuthCheckScreen(navController)
        }

        composable("welcomeScreen") {
            WelcomeScreen(
                modifier = Modifier.fillMaxSize(),
                onCreateAccountClicked = { navController.navigate("signUp") },
                onSignInClicked = { navController.navigate("login") }
            )
        }

        composable("signUp") {
            SignUpScreen(
                demoSignUpButtonClicked = {
                    navController.navigate("onboarding")
                },
                navController = navController
            )
        }


        composable("onboarding") {
            OnboardingScreen(navController = navController)
        }

        composable("login") {
            LoginScreen(navController = navController)
        }


        composable("mainMenuScreen") {
            MainMenuScreen(
                modifier = Modifier.fillMaxSize(),
                taskList = taskList,
                onAddClicked = { navController.navigate("addTaskScreen") }
            )
        }

        composable("addTaskScreen") {
            LaunchedEffect(true) {
                addTaskViewModel.resetTaskFields()
            }
            AddTaskScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = addTaskViewModel,
                onTaskAddClicked = {
                    taskList = taskList + it
                    navController.navigate("mainMenuScreen")
                },
                onCancelClicked = { navController.navigate("mainMenuScreen") }
            )
        }

        composable("calendar") {
            val context = LocalContext.current
            WeeklyCalendarScreen(navController = navController, context = context)
        }

        composable("addEvent") {
            AddEventScreen(navController = navController)
        }

        composable("integrateScreen") {
            //IntegrateScreen()
        }

        composable("notificationsScreen") {
            //NotificationsScreen()
        }

        composable("statisticsScreen") {
            //StatisticsScreen()
        }

        composable("categoryScreen") {
            //CategoryScreen()
        }

    }
}



@Composable
fun AddEventScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Add Event Screen")
    }
}


