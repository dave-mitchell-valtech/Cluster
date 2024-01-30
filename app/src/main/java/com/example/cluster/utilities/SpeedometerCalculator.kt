package com.example.cluster.utilities

import android.car.VehicleUnit
import android.graphics.PointF
import android.icu.util.MeasureUnit
import android.util.SizeF
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

class SpeedometerCalculator {

    fun getSpeedInfo(
        speed: Float,
        vehicleUnit: Int,
    ): Pair<MeasureUnit, Float> = let {
        val outputValue = speed * getSpeedRatio(vehicleUnit)
        when (vehicleUnit) {
            VehicleUnit.METER_PER_SEC -> MeasureUnit.METER_PER_SECOND to outputValue
            VehicleUnit.KILOMETERS_PER_HOUR -> MeasureUnit.KILOMETER_PER_HOUR to outputValue
            VehicleUnit.MILES_PER_HOUR -> MeasureUnit.MILE_PER_HOUR to outputValue
            else -> throw IllegalArgumentException("No support for unit=$vehicleUnit")
        }
    }.apply {
        if (second.absoluteValue != Float.POSITIVE_INFINITY) return@apply
        "Return type is insufficient to represent speed in the desired unit"
            .let { throw IllegalArgumentException(it) }
    }

    fun getSpeedRatio(vehicleUnit: Int): Float = when (vehicleUnit) {
        VehicleUnit.METER_PER_SEC -> 1f
        VehicleUnit.KILOMETERS_PER_HOUR -> 3.6f
        VehicleUnit.MILES_PER_HOUR -> 2.2369f
        else -> throw IllegalArgumentException("No support for unit=$vehicleUnit")
    }

    fun getPointOnCircle(center: PointF, radius: Float, degrees: Float): PointF = PointF(
        center.x + radius * cos(degrees),
        center.y + radius * sin(degrees),
    )
    fun getLineEndsX(
        longLength: Float,
        counter: Int,
        minorTicksPerMajorTick: Int,
    ): Pair<Float, Float> = when {
        counter % minorTicksPerMajorTick == 0 -> longLength * .7f to longLength
        else -> longLength * 0.8f to longLength
    }

    fun getMajorTickCount(
        maxRatedSpeed: Float,
        currentSpeedUnit: Int,
        preferredUnitsPerTick: Int,
    ): Int = getSpeedInfo(maxRatedSpeed, currentSpeedUnit)
        .run { second / preferredUnitsPerTick }
        .let(::ceil)
        .toInt()

    fun getTickWidth(counter: Int, minorTicksPerMajorTick: Int): Float = when {
        counter % minorTicksPerMajorTick == 0 -> 3f
        else -> 1f
    }

    fun getTickLabelOffset(
        tickLabel: String,
        speedPercentageOfMaxSpeed: Float,
        characterSize: SizeF,
    ): Pair<Float, Float> {
        val xOffsetBase = characterSize.width * tickLabel.length / 2f
        return when {
            speedPercentageOfMaxSpeed < 0.5f -> {
                val leftPercentage = speedPercentageOfMaxSpeed * 2f
                Pair(
                    lerp(0f, xOffsetBase, leftPercentage),
                    lerp(characterSize.height, 0f, leftPercentage),
                )
            }
            speedPercentageOfMaxSpeed > 0.5f -> {
                val rightPercentage = (speedPercentageOfMaxSpeed - 0.5f) * 2f
                Pair(
                    lerp(-xOffsetBase, 0f, rightPercentage),
                    lerp(0f, characterSize.height, rightPercentage),
                )
            }
            else -> 0f to 0f
        }
    }
}
