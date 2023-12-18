package com.example.cluster.ui

import android.app.Application
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.VehicleUnit
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.MeasureUnit
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import java.util.Locale

class ClusterViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val SENSOR_RATE = CarPropertyManager.SENSOR_RATE_UI
        private const val KEY_VEHICLE_SPEED = VehiclePropertyIds.PERF_VEHICLE_SPEED
        private const val KEY_VEHICLE_SPEED_DISPLAY_UNITS = VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS
        private val VEHICLE_PROPERTY_KEYS = listOf(
            KEY_VEHICLE_SPEED,
            KEY_VEHICLE_SPEED_DISPLAY_UNITS,
        )

        private const val EXCESSIVE_SPEED_THRESHOLD = 40
    }

    val normalColor = Color(0xFF33CCFF)
    val cautionColor = Color(0xFFF5CB40)
    val warningColor = Color(0xFFD32F2F)
    val currentSpeed = mutableStateOf(0f)
    val currentUnit = mutableStateOf(VehicleUnit.METER_PER_SEC)

    private val carPropertyManager = Car
        .createCar(application.applicationContext)
        .getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

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

    init {
        Log.d("TALLDAVE", "foo")
        VEHICLE_PROPERTY_KEYS.forEach { key ->
            carPropertyManager.registerCallback(carPropertyListener, key, SENSOR_RATE)
        }
    }

    fun getSpeedLabel(speed: Float, vehicleUnit: Int): String {
        val (preferredUnit, speedInPreferredUnit) = getSpeedInfo(speed, vehicleUnit)
        return NumberFormatter
            .withLocale(Locale.FRANCE)
            .notation(Notation.compactShort())
            .precision(Precision.maxFraction(0))
            .unit(preferredUnit)
            .format(speedInPreferredUnit)
            .toString()
    }

    fun getLabelAppearance(speed: Float, modifier: Modifier): Pair<Color, Modifier> {
        return when {
            speed > EXCESSIVE_SPEED_THRESHOLD -> warningColor to modifier
            speed < 0 -> Color.Unspecified to modifier.background(cautionColor)
            else -> Color.Unspecified to modifier
        }
    }

    fun getSpeedInfo(speed: Float, vehicleUnit: Int): Pair<MeasureUnit, Float> {
        return when (vehicleUnit) {
            VehicleUnit.METER_PER_SEC -> MeasureUnit.METER_PER_SECOND to speed
            VehicleUnit.KILOMETERS_PER_HOUR -> MeasureUnit.KILOMETER_PER_HOUR to speed * 3.6f
            VehicleUnit.MILES_PER_HOUR -> MeasureUnit.MILE_PER_HOUR to speed * 2.2369f
            else -> throw IllegalArgumentException("No support for unit=$vehicleUnit")
        }
    }
}
