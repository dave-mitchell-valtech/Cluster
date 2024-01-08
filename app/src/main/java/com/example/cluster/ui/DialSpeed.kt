package com.example.cluster.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cluster.utilities.SpeedometerCalculator

private val calculator = SpeedometerCalculator()

@Composable
fun DialSpeed(
    speed: MutableState<Float>,
    speedUnit: MutableState<Int>,
    mainColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    preferredUnitsPerMajorTick: Int = 20,
    minorTicksPerMajorTick: Int = 4,
    maxRatedSpeed: Float = 80f,
    arcDegreeRange: Float = 270f,
) {
    val majorTickCount = calculator.getMajorTickCount(
        maxRatedSpeed,
        speedUnit.value,
        preferredUnitsPerMajorTick,
    )
    val tickCount = majorTickCount * minorTicksPerMajorTick
    val maxSpeedInPreferredUnit = majorTickCount * preferredUnitsPerMajorTick
    val maxSpeed = maxSpeedInPreferredUnit / calculator.getSpeedRatio(speedUnit.value)
    val foregroundColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.wrapContentSize()) {
        DialSpeedIndication(
            speed = speed,
            maxSpeed = maxSpeed,
            mainColor = mainColor,
            secondaryColor = secondaryColor,
            foregroundColor = foregroundColor,
            arcDegreeRange = arcDegreeRange,
        )
        DialSpeedTicks(
            mainColor = mainColor,
            arcDegreeRange = arcDegreeRange,
            tickCount = tickCount,
        )
        DialSpeedTickLabels(
            maxSpeedInPreferredUnit = maxSpeedInPreferredUnit,
            arcDegreeRange = arcDegreeRange,
            preferredUnitsPerMajorTick = preferredUnitsPerMajorTick,
            foregroundColor = foregroundColor,
        )
    }
}
