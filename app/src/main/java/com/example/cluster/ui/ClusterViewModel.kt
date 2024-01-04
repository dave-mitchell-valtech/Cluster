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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import java.util.Locale

class ClusterViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private val TAG = ClusterViewModel::class.java.simpleName

        private const val SENSOR_RATE = CarPropertyManager.SENSOR_RATE_UI
        private const val KEY_VEHICLE_SPEED = VehiclePropertyIds.PERF_VEHICLE_SPEED
        private const val KEY_VEHICLE_SPEED_DISPLAY_UNITS = VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS
        private val VEHICLE_PROPERTY_KEYS = listOf(
            KEY_VEHICLE_SPEED,
            KEY_VEHICLE_SPEED_DISPLAY_UNITS,
        )

        private val COLOR_NORMAL = Color(0xFF33CCFF)
        private val COLOR_CAUTION = Color(0xFFF5CB40)
        private val COLOR_WARNING = Color(0xFFD32F2F)

        private const val CAUTION_SPEED_THRESHOLD = 38f
        private const val WARNING_SPEED_THRESHOLD = 40f
    }

    val currentSpeed = mutableStateOf(0f)
    val currentSpeedUnit = mutableStateOf(VehicleUnit.METER_PER_SEC)

    private val carPropertyManager = Car
        .createCar(application.applicationContext)
        .getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

    private val carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(p0: CarPropertyValue<*>?) {
            when (p0?.propertyId) {
                KEY_VEHICLE_SPEED -> currentSpeed.value = p0.value as Float
                KEY_VEHICLE_SPEED_DISPLAY_UNITS -> currentSpeedUnit.value = p0.value as Int
            }
        }

        override fun onErrorEvent(p0: Int, p1: Int) {
            Log.e(TAG, "Error setting vehicle property $p0 zone $p1")
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int, errorCode: Int) {
            super.onErrorEvent(propertyId, areaId, errorCode)
            "Error setting vehicle property $propertyId areaId $areaId (error code $errorCode)"
                .let { Log.e(TAG, it) }
        }
    }

    init {
        VEHICLE_PROPERTY_KEYS.forEach { key ->
            carPropertyManager.registerCallback(carPropertyListener, key, SENSOR_RATE)
        }
    }

    fun getSpeedLabel(speedInPreferredUnit: Float, preferredUnit: MeasureUnit): String {
        return NumberFormatter
            .withLocale(Locale.getDefault())
            .notation(Notation.compactShort())
            .precision(Precision.maxFraction(0))
            .unit(preferredUnit)
            .format(speedInPreferredUnit)
            .toString()
    }

    fun getSpeedLabelColors(speed: Float) = when {
        speed < 0f -> Color.Black to COLOR_CAUTION
        else -> Color.Unspecified to Color.Unspecified
    }

    fun getDialColors(absoluteSpeed: Float) = when {
        absoluteSpeed < CAUTION_SPEED_THRESHOLD -> COLOR_NORMAL
        absoluteSpeed < WARNING_SPEED_THRESHOLD -> COLOR_CAUTION
        else -> COLOR_WARNING
    }.let {
        it to it.copy(alpha = 0.2f)
    }
}
