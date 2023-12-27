package com.example.cluster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cluster.ui.DigitalSpeed
import com.example.cluster.ui.Speedometer
import com.example.cluster.ui.theme.ClusterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClusterTheme() {
                Surface {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxSize(),
                    ) {
                        Speedometer()
                        DigitalSpeed()
                    }
                }
            }
        }
    }
}
