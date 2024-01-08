package com.example.cluster

import android.app.Application
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import com.example.cluster.data.VehiclePropertyRepository

class ClusterApp : Application() {

    private val carPropertyManager by lazy {
        Car.createCar(applicationContext)
            .getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    val vehiclePropertyRepository by lazy {
        VehiclePropertyRepository(carPropertyManager)
    }
}
