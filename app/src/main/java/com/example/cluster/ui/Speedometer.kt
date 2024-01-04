package com.example.cluster.ui

import android.graphics.PointF
import android.util.SizeF
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.absoluteValue

private const val CHARACTER_WIDTH = 10f
private const val CHARACTER_HEIGHT = 12f

@Composable
fun Speedometer(
    speed: MutableState<Float>,
    speedUnit: MutableState<Int>,
    mainColor: Color,
    secondaryColor: Color,
    preferredUnitsPerMajorTick: Int = 20,
    minorTicksPerMajorTick: Int = 4,
    maxRatedSpeed: Float = 80f,
    arcDegreeRange: Float = 270f,
    calculator: SpeedometerCalculator = SpeedometerCalculator(),
) {
    val currentSpeed by speed
    val currentSpeedUnit by speedUnit

    // Derived configuration
    val majorTickCount = calculator.getMajorTickCount(maxRatedSpeed, currentSpeedUnit, preferredUnitsPerMajorTick)
    val maxSpeedInPreferredUnit = majorTickCount * preferredUnitsPerMajorTick
    val maxSpeed = maxSpeedInPreferredUnit / calculator.getSpeedRatio(currentSpeedUnit)
    val tickCount = majorTickCount * minorTicksPerMajorTick
    val degreesMarkerStep = arcDegreeRange / tickCount
    val (startArcAngle, startStepAngle) = ((arcDegreeRange - 180) / 2f)
        .let { (180 - it) to -it.toInt() }

    // Other representations of speed
    val interpolatedAbsoluteSpeed by animateFloatAsState(
        label = "speed",
        targetValue = currentSpeed.absoluteValue,
        animationSpec = spring(
            dampingRatio = 1f,
            stiffness = 200f,
        ),
    )
    val interpolatedAbsoluteSpeedDegree = lerp(
        0f,
        arcDegreeRange,
        (interpolatedAbsoluteSpeed / maxSpeed).coerceAtMost(1f),
    )
    val degrees = ((startStepAngle - 180 + interpolatedAbsoluteSpeedDegree) * PI / 180f).toFloat()

    // Colors
    val mainPaint = Paint().apply { color = mainColor }
    val pointerPaint = Paint().apply { color = MaterialTheme.colorScheme.onSurface }

    Canvas(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onDraw = {
            // Sizes and positions
            val center = PointF(drawContext.size.width / 2f, drawContext.size.height / 2f)
            val centerOffset = drawContext.size.center
            val quarterOffset = centerOffset / 2f
            val centerArcSize = drawContext.size / 2f
            val tickBaseLength = centerOffset.x / 2.8f
            val tickLabelRadius = centerArcSize.width * 0.84f
            val centerArcStroke = Stroke(centerOffset.x / 10f, 0f, StrokeCap.Butt)
            val pointerLength = centerArcSize.width / 2f
            val pointerFirstPoint = calculator.getPointOnCircle(center, 5f, degrees - 90f)
            val pointerSecondPoint = calculator.getPointOnCircle(center, 5f, degrees + 90f)
            val pointerFarPoint = calculator.getPointOnCircle(center, pointerLength, degrees)

            // Available progress range
            drawArc(
                secondaryColor,
                startArcAngle,
                arcDegreeRange,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke,
            )

            // Progress
            drawArc(
                mainColor,
                startArcAngle,
                interpolatedAbsoluteSpeedDegree,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke,
            )

            // Pointer
            drawCircle(pointerPaint.color, 14f, centerOffset)
            drawPath(
                Path().apply {
                    moveTo(pointerFirstPoint.x, pointerFirstPoint.y)
                    lineTo(pointerSecondPoint.x, pointerSecondPoint.y)
                    lineTo(pointerFarPoint.x, pointerFarPoint.y)
                    lineTo(pointerFirstPoint.x, pointerFirstPoint.y)
                    close()
                },
                pointerPaint.color,
            )

            drawIntoCanvas { canvas ->
                // Drawing tick marks
                (0..tickCount).forEach { counter ->
                    val rotation = counter * degreesMarkerStep + startStepAngle
                    val (lineStartX, lineEndX) = calculator
                        .getLineEndsX(tickBaseLength, counter, minorTicksPerMajorTick)
                    mainPaint.strokeWidth = calculator.getTickWidth(counter, minorTicksPerMajorTick)
                    canvas.run {
                        save()
                        rotate(rotation, centerOffset.x, centerOffset.y)
                        drawLine(
                            Offset(lineStartX, centerOffset.y),
                            Offset(lineEndX, centerOffset.y),
                            mainPaint,
                        )
                        restore()
                    }
                }

                // Speed tick labels
                val rangeOfPreferredUnits = 0..maxSpeedInPreferredUnit
                for (speedInPreferredUnit in rangeOfPreferredUnits step preferredUnitsPerMajorTick) {
                    val speedPercentageOfMaxSpeed = (speedInPreferredUnit / maxSpeedInPreferredUnit.toFloat())
                        .coerceAtMost(1f)

                    val textPosition = lerp(0f, arcDegreeRange, speedPercentageOfMaxSpeed)
                        .let { ((startStepAngle - 180 + it) * PI / 180f).toFloat() }
                        .let { calculator.getPointOnCircle(center, tickLabelRadius, it) }
                    val (xOffset, yOffset) = calculator.getTickLabelOffset(
                        speedInPreferredUnit.toString(),
                        speedPercentageOfMaxSpeed,
                        SizeF(CHARACTER_WIDTH, CHARACTER_HEIGHT),
                    )
                    val tickLabelPaint = Paint().asFrameworkPaint().apply {
                        color = pointerPaint.color.toArgb()
                        textSize = tickBaseLength * 0.3f
                        textAlign = when {
                            speedPercentageOfMaxSpeed < 0.5f -> android.graphics.Paint.Align.RIGHT
                            speedPercentageOfMaxSpeed > 0.5f -> android.graphics.Paint.Align.LEFT
                            else -> android.graphics.Paint.Align.CENTER
                        }
                    }
                    canvas.nativeCanvas.drawText(
                        "$speedInPreferredUnit",
                        textPosition.x + xOffset,
                        textPosition.y + yOffset,
                        tickLabelPaint,
                    )
                }
            }
        },
    )
}
