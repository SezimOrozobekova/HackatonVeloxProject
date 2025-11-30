package com.example.velox.commonCompose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.velox.R

@Composable
fun FilledButton(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors().copy(containerColor = colorResource(R.color.button_container)),
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    shape: Shape = RoundedCornerShape(10.dp),
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: TextUnit = 18.sp,
    textColor: Color = Color.White,
    name: String,
){
    Button(
        modifier = modifier,
        colors = buttonColors,
        contentPadding = contentPadding,
        shape = shape,
        onClick = onClicked
    ){
        Text(
            text = name,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = textColor
        )
    }
}

@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    name: String? = null,
    image: Painter? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    border: BorderStroke? = BorderStroke(width = 1.dp, color = Color.White),
    shape: Shape = RoundedCornerShape(10.dp),
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: TextUnit = 18.sp,
    textColor: Color = Color.White,
    onClicked: () -> Unit
){
    Button(
        modifier = modifier,
        shape = shape,
        border = border,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent),
        onClick = onClicked
    ){
        name?.let {
            Text(
                text = name,
                fontWeight = fontWeight,
                fontSize = fontSize,
                color = textColor
            )
        }
        image?.let {
            Image(
                painter = it,
                contentDescription = null
            )
        }
    }
}