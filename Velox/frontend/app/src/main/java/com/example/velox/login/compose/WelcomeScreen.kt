package com.example.velox.login.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.velox.R
import com.example.velox.commonCompose.FilledButton
import com.example.velox.commonCompose.TextButton
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, onSignInClicked: () -> Unit, onCreateAccountClicked: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(colorResource(R.color.background_starter))
    Box(
        modifier = modifier.background(colorResource(R.color.background_starter)),
        contentAlignment = Alignment.TopCenter
    ){
        Box(modifier = Modifier.fillMaxSize(0.6f), contentAlignment = Alignment.TopCenter){
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.image_lion),
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 120.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            WellcomeText(Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            CustomButton(
                modifier = Modifier.fillMaxWidth(),
                onCreateAccountClicked = onCreateAccountClicked,
                onSignInClicked = onSignInClicked
            )
        }
    }
}

@Composable
private fun CustomButton(
    modifier: Modifier = Modifier,
    onCreateAccountClicked: () -> Unit,
    onSignInClicked: () -> Unit,
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        FilledButton(
            modifier = Modifier.fillMaxWidth(),
            onClicked = onSignInClicked,
            name = "Sign in",
        )
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            name = "Create account",
            onClicked = onCreateAccountClicked
        )
    }
}


@Composable
private fun WellcomeText(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Hello and welcome!",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color.White
        )
        Text(
            text = "Explore, create, and experience the app that’s built around you.",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,

        )
    }
}