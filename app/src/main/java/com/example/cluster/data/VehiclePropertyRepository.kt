package com.example.cluster.data

import android.car.VehiclePropertyIds
import android.car.VehicleUnit
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface VehiclePropertyRepository {
    val speed: MutableStateFlow<Float>
    val speedUnit: MutableStateFlow<Int>
    val currentSpeed: StateFlow<Float>
    val currentSpeedUnit: StateFlow<Int>
    var paused: Boolean
}

class RealVehiclePropertyRepository(
    private val carPropertyManager: CarPropertyManager,
    override val speed: MutableStateFlow<Float> = MutableStateFlow(0f),
    override val speedUnit: MutableStateFlow<Int> = MutableStateFlow(VehicleUnit.MILES_PER_HOUR),
    override var paused: Boolean = false,
) : VehiclePropertyRepository {
    companion object {
        private val TAG = VehiclePropertyRepository::class.java.simpleName

        private const val SENSOR_RATE = CarPropertyManager.SENSOR_RATE_ONCHANGE
        private const val KEY_VEHICLE_SPEED = VehiclePropertyIds.PERF_VEHICLE_SPEED
        private const val KEY_VEHICLE_SPEED_UNITS = VehiclePropertyIds.VEHICLE_SPEED_DISPLAY_UNITS
        private val VEHICLE_PROPERTY_KEYS = listOf(
            KEY_VEHICLE_SPEED,
            KEY_VEHICLE_SPEED_UNITS,
        )
    }

    override val currentSpeed = speed.asStateFlow()
    override val currentSpeedUnit = speedUnit.asStateFlow()

    private val carPropertyListener = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(p0: CarPropertyValue<*>?) {
            if (paused) return
            when (p0?.propertyId) {
                KEY_VEHICLE_SPEED -> speed.value = p0.value as Float
                KEY_VEHICLE_SPEED_UNITS -> speedUnit.value = p0.value as Int
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
}

class MockVehiclePropertyRepository(
    override val speed: MutableStateFlow<Float> = MutableStateFlow(0f),
    override val speedUnit: MutableStateFlow<Int> = MutableStateFlow(VehicleUnit.MILES_PER_HOUR),
    override var paused: Boolean = false,
) : VehiclePropertyRepository {
    override val currentSpeed = speed.asStateFlow()
    override val currentSpeedUnit = speedUnit.asStateFlow()
}
