package com.example.cluster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cluster.ui.DialSpeed
import com.example.cluster.ui.DigitalSpeed
import com.example.cluster.ui.theme.ClusterTheme
import com.example.cluster.viewmodels.ClusterViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClusterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column {
                        (viewModel() as ClusterViewModel).run {
                            Box(modifier = Modifier.wrapContentSize().clickable(onClick = ::demo)) {
                                DialSpeed(currentSpeed, currentSpeedUnit, mainColor, secondaryColor)
                                DigitalSpeed(speedLabel, unitLabel, textColor, backgroundColor)
                                demo()
                            }
                        }
                    }
                }
            }
        }
    }
}
