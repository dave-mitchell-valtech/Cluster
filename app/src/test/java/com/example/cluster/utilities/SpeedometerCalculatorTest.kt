package com.example.cluster.utilities

import android.car.VehicleUnit
import android.graphics.PointF
import android.icu.util.MeasureUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner

class Spec(
    val expectedMeasureUnit: MeasureUnit,
    val expectedRatio: Float,
)

private val sut = SpeedometerCalculator()

@RunWith(Enclosed::class)
class SpeedometerCalculatorTest {

    @RunWith(ParameterizedRobolectricTestRunner::class)
    class UnitConversion(
        private val unit: Int,
        private val description: String,
    ) {
        companion object {
            private const val VEHICLE_SPEED_TOO_BIG = Float.POSITIVE_INFINITY

            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
            fun data() = listOf(
                arrayOf(VehicleUnit.METER_PER_SEC, "m/s"),
                arrayOf(VehicleUnit.MILES_PER_HOUR, "mi/h"),
                arrayOf(VehicleUnit.KILOMETERS_PER_HOUR, "km/h"),
            )
        }

        private val spec by lazy {
            mapOf(
                VehicleUnit.METER_PER_SEC to Spec(MeasureUnit.METER_PER_SECOND, 1f),
                VehicleUnit.MILES_PER_HOUR to Spec(MeasureUnit.MILE_PER_HOUR, 2.2369f),
                VehicleUnit.KILOMETERS_PER_HOUR to Spec(MeasureUnit.KILOMETER_PER_HOUR, 3.6f),
            )[unit]!!
        }

        @Test
        fun getSpeedInfoTranslateVehicleUnitToMeasureUnit() {
            assertEquals(
                "Resulting unit for $description is incorrect.",
                spec.expectedMeasureUnit,
                sut.getSpeedInfo(0f, unit).first,
            )
        }

        @Test
        fun getSpeedInfoPreserveSign() {
            val result = sut.getSpeedInfo(-1f, unit).second
            assertTrue(
                "Conversion to $description ignored negative unit sign.",
                result < 0f,
            )
        }

        @Test
        fun getSpeedInfoThrowIfResultingSpeedExceedsTypeRange() {
            assertThrows(
                "Speed in $description is out of range for the return type.",
                Throwable::class.java,
            ) { sut.getSpeedInfo(VEHICLE_SPEED_TOO_BIG, unit) }
        }

        @Test
        fun getSpeedInfoSpeedConversion() {
            val input = 5f
            assertEquals(
                "Conversion from $input m/s to $description is incorrect.",
                input * spec.expectedRatio,
                sut.getSpeedInfo(input, unit).second,
            )
        }

        @Test
        fun getSpeedRatioReturnsCorrectValue() {
            assertEquals(
                "Speed ratio for $description is incorrect.",
                spec.expectedRatio,
                sut.getSpeedRatio(unit),
            )
        }
    }

    class UnsupportedUsageFeedback {
        companion object {
            private const val VEHICLE_SPEED_UNIT_INVALID = -1
            private const val VEHICLE_SPEED_UNIT_NOT_SUPPORTED = VehicleUnit.PSI
        }

        @Test
        fun getSpeedInfoThrowIfVehicleUnitNotSupported() {
            assertThrows(
                "Caller not informed of speed info request for an unsupported unit type",
                Throwable::class.java,
            ) {
                sut.getSpeedInfo(0f, VEHICLE_SPEED_UNIT_NOT_SUPPORTED)
            }
        }

        @Test
        fun getSpeedInfoThrowIfVehicleUnitInvalid() {
            assertThrows(
                "Caller not informed of speed info request for an invalid unit type",
                Throwable::class.java,
            ) {
                sut.getSpeedInfo(0f, VEHICLE_SPEED_UNIT_INVALID)
            }
        }

        @Test
        fun getSpeedRatioThrowIfVehicleUnitNotSupported() {
            assertThrows(
                "Caller not informed of speed ratio request for an unsupported unit type",
                Throwable::class.java,
            ) {
                sut.getSpeedRatio(VEHICLE_SPEED_UNIT_NOT_SUPPORTED)
            }
        }

        @Test
        fun getSpeedRatioThrowIfVehicleUnitInvalid() {
            assertThrows(
                "Caller not informed of speed ratio request for an invalid unit type",
                Throwable::class.java,
            ) {
                sut.getSpeedRatio(VEHICLE_SPEED_UNIT_INVALID)
            }
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class DrawingUtilities {
        @Test
        fun getPointOnCircleRightEdge() {
            val center = PointF(500f, 500f)
            val radius = 250f

            assertEquals(
                "Resulting coordinates are incorrect",
                PointF(center).apply { x += radius },
                sut.getPointOnCircle(center, radius, 0f),
            )
        }

        @Test
        fun getPointOnCircle45Degrees() {
            assertEquals(
                "Resulting coordinates are incorrect",
                PointF(631.3305f, 712.7259f),
                sut.getPointOnCircle(PointF(500f, 500f), 250f, 45f),
            )
        }

        @Test
        fun getPointOnCircleNegative() {
            val result = sut.getPointOnCircle(PointF(0f, 0f), 10f, 180f)
            assertTrue(
                "Resulting coordinates $result should be negative",
                result.x < 0 && result.y < 0,
            )
        }
    }
}
