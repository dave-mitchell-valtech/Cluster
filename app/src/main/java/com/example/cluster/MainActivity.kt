package com.example.cluster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.cluster.ui.ClusterViewModel
import com.example.cluster.ui.DigitalSpeed
import com.example.cluster.ui.Speedometer
import com.example.cluster.ui.SpeedometerCalculator
import com.example.cluster.ui.theme.ClusterTheme
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cvm: ClusterViewModel = ViewModelProvider(this)[ClusterViewModel::class.java]
        val absoluteSpeed = cvm.currentSpeed.value.absoluteValue
        val (preferredSpeedUnit, speedInPreferredUnit) = SpeedometerCalculator()
            .getSpeedInfo(absoluteSpeed, cvm.currentSpeedUnit.value)
        val (speedLabel, unitLabel) = cvm
            .getSpeedLabel(speedInPreferredUnit, preferredSpeedUnit)
            .split("\\s+".toRegex())
            .let {
                when (it.size > 1) {
                    true -> Pair(it[0], it[1])
                    else -> Pair(it[0], "???")
                }
            }
        val (textColor, backgroundColor) = cvm.getSpeedLabelColors(cvm.currentSpeed.value)
        val (mainColor, secondaryColor) = cvm.getDialColors(absoluteSpeed)

        setContent {
            ClusterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.wrapContentSize()) {
                        Speedometer(cvm.currentSpeed, cvm.currentSpeedUnit, mainColor, secondaryColor)
                        DigitalSpeed(speedLabel, unitLabel, textColor, backgroundColor)
                    }
                }
            }
        }
    }
}
