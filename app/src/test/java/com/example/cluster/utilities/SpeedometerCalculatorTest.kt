package com.example.cluster.utilities

import android.car.VehicleUnit
import android.icu.util.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.absoluteValue

//@RunWith(ParameterizedRobolectricTestRunner::class)
class SpeedometerCalculatorTest(
    private val unit: Int = 0,
    private val description: String = "test",
    private val expectedCorrespondingMeasureUnit: MeasureUnit = MeasureUnit.MILE_PER_HOUR,
    private val expectedConversionRatio: Float = 1f,
) {
    companion object {
        private const val VEHICLE_SPEED_UNIT_INVALID = -1
        private const val VEHICLE_SPEED_UNIT_NOT_SUPPORTED = VehicleUnit.PSI
        private const val VEHICLE_SPEED_TOO_BIG = Float.MAX_VALUE

        @JvmStatic
        //@ParameterizedRobolectricTestRunner.Parameters()
        fun params() = listOf(
            arrayOf(VehicleUnit.METER_PER_SEC, "meters per second", MeasureUnit.METER_PER_SECOND, 1f),
            arrayOf(VehicleUnit.MILES_PER_HOUR, "miles per hour", MeasureUnit.MILE_PER_HOUR, 1f),
            arrayOf(VehicleUnit.KILOMETERS_PER_HOUR, "kilometers per hour", MeasureUnit.KILOMETER_PER_HOUR, 1f),
        )
    }

    private val sut = SpeedometerCalculator()

    // Normal usage

//    @Test
//    fun getSpeedInfoTranslateVehicleUnitToMeasureUnitMph() {
//        val result = sut.getSpeedInfo(0f, VehicleUnit.MILES_PER_HOUR).first
//        assertEquals(result, MeasureUnit.MILE_PER_HOUR)
//    }
//
//    @Test
//    fun getSpeedInfoTranslateVehicleUnitToMeasureUnitKph() {
//        val result = sut.getSpeedInfo(0f, VehicleUnit.KILOMETERS_PER_HOUR).first
//        assertEquals(result, MeasureUnit.KILOMETER_PER_HOUR)
//    }
//
//    @Test
//    fun getSpeedInfoTranslateVehicleUnitToMeasureUnitMps() {
//        val result = sut.getSpeedInfo(0f, VehicleUnit.METER_PER_SEC).first
//        assertEquals(result, MeasureUnit.METER_PER_SECOND)
//    }

    @Test
    fun getSpeedInfoPreserveSign() {
        val result = sut.getSpeedInfo(-1f, unit).second
        assertEquals(
            "Conversion to $description ignored negative sign.",
            -result.absoluteValue,
            result,
        )
    }

    // Unsupported usage

//    @Test
//    fun getSpeedInfoThrowIfVehicleUnitNotSupported() {
//        assertThrows(Throwable::class.java) {
//            sut.getSpeedInfo(0f, VEHICLE_SPEED_UNIT_NOT_SUPPORTED)
//        }
//    }
//
//    @Test
//    fun getSpeedInfoThrowIfVehicleUnitInvalid() {
//        assertThrows(Throwable::class.java) {
//            sut.getSpeedInfo(0f, VEHICLE_SPEED_UNIT_INVALID)
//        }
//    }
//
//    @Test
//    fun getSpeedInfoThrowIfMpsToMphTooBig() {
//        assertThrows(Throwable::class.java) {
//            sut.getSpeedInfo(VEHICLE_SPEED_TOO_BIG, VehicleUnit.MILES_PER_HOUR)
//        }
//    }
//
//    @Test
//    fun getSpeedRatio() {
//        // val result = sut.getSpeedRatio()
//    }
//
//    @Test
//    fun getPointOnCircle() {
//    }
//
//    @Test
//    fun getLineEndsX() {
//    }
//
//    @Test
//    fun getMajorTickCount() {
//    }
//
//    @Test
//    fun getTickWidth() {
//    }
//
//    @Test
//    fun getTickLabelOffset() {
//    }
}
