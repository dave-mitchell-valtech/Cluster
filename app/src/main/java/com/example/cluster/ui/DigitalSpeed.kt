package com.example.cluster.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.absoluteValue

@Composable
fun DigitalSpeed(
    modifier: Modifier = Modifier,
    clusterViewModel: ClusterViewModel = viewModel(),
) {
    val currentSpeed by remember { clusterViewModel.currentSpeed }
    val currentUnit by remember { clusterViewModel.currentUnit }
    val (speedLabel, unitLabel) = clusterViewModel
        .getSpeedLabel(currentSpeed.absoluteValue, currentUnit)
        .split("\\s+".toRegex())
        .let {
            when (it.size > 1) {
                true -> Pair(it[0], it[1])
                else -> Pair(it[0], "???")
            }
        }
    val (textColor, textModifier) = clusterViewModel.getLabelAppearance(currentSpeed, modifier)
    Text(
        text = speedLabel,
        color = textColor,
        modifier = textModifier.padding(horizontal = 20.dp),
        style = TextStyle(
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
        ),
    )

    Text(
        text = unitLabel,
        color = textColor,
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}
