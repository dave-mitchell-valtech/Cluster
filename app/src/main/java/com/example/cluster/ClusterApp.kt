package com.example.cluster

import android.app.Application
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import com.example.cluster.data.RealVehiclePropertyRepository

class ClusterApp : Application() {

    private val car by lazy {
        Car.createCar(applicationContext)
    }

    private val carPropertyManager by lazy {
        car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    val vehiclePropertyRepository by lazy {
        RealVehiclePropertyRepository(carPropertyManager)
    }
}
