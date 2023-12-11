package com.example.cluster

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.VehicleUnit
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.MeasureUnit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cluster.ui.theme.ClusterTheme
import java.util.Locale

private val currentSpeed = mutableStateOf(0f)
private val currentUnit = mutableStateOf(VehicleUnit.METER_PER_SEC)

class MainActivity : ComponentActivity() {

    companion object {
        private const val SENSOR_RATE = CarPropertyManager.SENSOR_RATE_UI
        private const val KEY_VEHICLE_SPEED = VehiclePropertyIds.PERF_VEHICLE_SPEED
        private const val KEY_VEHICLE_SPEED_DISPLAY_UNITS = VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS
        private val VEHICLE_PROPERTY_KEYS = listOf(
            KEY_VEHICLE_SPEED,
            KEY_VEHICLE_SPEED_DISPLAY_UNITS,
        )
    }

    private val carPropertyManager by lazy {
        Car.createCar(applicationContext).getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }
    private val carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(p0: CarPropertyValue<*>?) {
            when (p0?.propertyId) {
                KEY_VEHICLE_SPEED -> currentSpeed.value = p0.value as Float
                KEY_VEHICLE_SPEED_DISPLAY_UNITS -> currentUnit.value = p0.value as Int
            }
        }

        override fun onErrorEvent(p0: Int, p1: Int) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VEHICLE_PROPERTY_KEYS.forEach { key ->
            carPropertyManager.registerCallback(carPropertyListener, key, SENSOR_RATE)
        }
        setContent {
            ClusterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DigitalSpeed(
                        currentSpeed,
                        currentUnit,
                        modifier = Modifier.wrapContentSize(),
                    )
                }
            }
        }
    }
}

private const val EXCESSIVE_SPEED_THRESHOLD = 40
private const val REVERSE_SPEED_THRESHOLD = 0

@Composable
fun DigitalSpeed(speed: MutableState<Float>, unit: MutableState<Int>, modifier: Modifier = Modifier) {
    val currentSpeed by remember { speed }
    val currentUnit by remember { unit }
    val (preferredUnit, speedInPreferredUnit) = when (currentUnit) {
        VehicleUnit.METER_PER_SEC -> Pair(MeasureUnit.METER_PER_SECOND, currentSpeed)
        VehicleUnit.KILOMETERS_PER_HOUR -> Pair(MeasureUnit.KILOMETER_PER_HOUR, currentSpeed * 3.6f)
        VehicleUnit.MILES_PER_HOUR -> Pair(MeasureUnit.MILE_PER_HOUR, currentSpeed * 2.2369f)
        else -> throw IllegalArgumentException("No support for unit=$currentUnit")
    }
    val speedLabel = NumberFormatter.withLocale(Locale.getDefault())
        .notation(Notation.compactShort())
        .unit(preferredUnit)
        .precision(Precision.minMaxFraction(2, 2))
        .format(speedInPreferredUnit)
        .toString()
    val (textColor, textModifier) = when {
        currentSpeed > EXCESSIVE_SPEED_THRESHOLD -> Pair(Color.Red, modifier)
        currentSpeed < REVERSE_SPEED_THRESHOLD -> Pair(Color.Unspecified, modifier.background(Color.Yellow))
        else -> Pair(Color.Unspecified, modifier)
    }
    Text(
        text = speedLabel,
        color = textColor,
        modifier = textModifier,
    )
}
