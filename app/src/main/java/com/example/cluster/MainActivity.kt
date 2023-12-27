package com.example.cluster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cluster.ui.DigitalSpeed
import com.example.cluster.ui.Speedometer
import com.example.cluster.ui.theme.ClusterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClusterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.wrapContentSize()) {
                        Speedometer()
                        DigitalSpeed()
                    }
                }
            }
        }
    }
}
