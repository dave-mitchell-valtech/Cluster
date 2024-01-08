package com.example.cluster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DigitalSpeed(
    speedLabel: MutableState<String>,
    unitLabel: MutableState<String>,
    textColor: MutableState<Color>,
    backgroundColor: MutableState<Color>,
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .wrapContentSize()
            .fillMaxSize(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = speedLabel.value,
            color = textColor.value,
            modifier = Modifier
                .background(backgroundColor.value)
                .padding(horizontal = 20.dp),
            style = TextStyle(
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = unitLabel.value,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
