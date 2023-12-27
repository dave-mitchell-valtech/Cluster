package com.example.cluster.ui

import android.graphics.PointF
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Speedometer(
    maxTheoreticalSpeed: Float = 80f,
    arcDegreeRange: Float = 270f,
    clusterViewModel: ClusterViewModel = viewModel(),
) {
    val currentSpeed by remember { clusterViewModel.currentSpeed }
    val currentUnit by remember { clusterViewModel.currentUnit }

    // Static properties
    val preferredUnitsPerTick = 20
    val minorTicksPerMajorTick = 5

    val majorTickCount = clusterViewModel
        .getSpeedInfo(maxTheoreticalSpeed, currentUnit)
        .second
        .let { it / preferredUnitsPerTick }
        .let(::ceil)
        .toInt()
    val speedRatio = clusterViewModel.getSpeedRatio(currentUnit)
    val maxSpeed = majorTickCount * preferredUnitsPerTick / speedRatio
    val tickCount = majorTickCount * minorTicksPerMajorTick
    val (startArcAngle, startStepAngle) = ((arcDegreeRange - 180) / 2f)
        .let { (180 - it) to (-it.toInt()) }

    // Other representations of speed
    val absoluteSpeed = currentSpeed.absoluteValue
    val interpolatedAbsoluteSpeed by animateFloatAsState(
        label = "speed",
        targetValue = absoluteSpeed,
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
    val (mainColor, secondaryColor) = when {
        absoluteSpeed < 38 -> clusterViewModel.normalColor
        absoluteSpeed < 40 -> clusterViewModel.cautionColor
        else -> clusterViewModel.warningColor
    }.let {
        it to it.copy(alpha = 0.2f)
    }
    val paintMainColor = Paint().apply { color = mainColor }

    // Helpers
    val getPointOnCircle = fun(center: PointF, radius: Float, degrees: Float): PointF = PointF(
        center.x + radius * cos(degrees),
        center.y + radius * sin(degrees),
    )
    val getLineEndsX = fun(longLength: Float, counter: Int): Pair<Float, Float> = when {
        counter % 5 == 0 -> 3f to longLength * .7f
        else -> 1f to longLength * 0.8f
    }.let {
        paintMainColor.strokeWidth = it.first
        it.second to longLength
    }

    Canvas(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onDraw = {
            // Sizes and positions
            val center = PointF(drawContext.size.width / 2f, drawContext.size.height / 2f)
            val centerOffset = Offset(center.x, center.y)
            val quarterOffset = Offset(center.x / 2f, center.y / 2f)
            val centerArcSize = Size(center.x, center.y)
            val tickBaseLength = center.x / 2.8f
            val centerArcStroke = Stroke(center.x / 10f, 0f, StrokeCap.Butt)

            val firstPoint = getPointOnCircle(center, 5f, degrees - 90f)
            val secondPoint = getPointOnCircle(center, 5f, degrees + 90f)
            val farPoint = getPointOnCircle(center, (centerArcSize.width / 2f), degrees)

            drawIntoCanvas { canvas ->
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
                val pointerColor = Color.Black
                val pointerPaint = Paint().apply { color = pointerColor }
                drawCircle(pointerColor, 14f, centerOffset)
                canvas.drawPath(
                    Path().apply {
                        moveTo(firstPoint.x, firstPoint.y)
                        lineTo(secondPoint.x, secondPoint.y)
                        lineTo(farPoint.x, farPoint.y)
                        lineTo(firstPoint.x, firstPoint.y)
                        close()
                    },
                    pointerPaint,
                )

                // Drawing tic marks
                (0..tickCount).forEach { counter ->
                    val degreesMarkerStep = arcDegreeRange / tickCount
                    val rotation = counter * degreesMarkerStep + startStepAngle
                    val (lineStartX, lineEndX) = getLineEndsX(tickBaseLength, counter)
                    canvas.run {
                        save()
                        rotate(rotation, center.x, center.y)
                        drawLine(
                            Offset(lineStartX, center.y),
                            Offset(lineEndX, center.y),
                            paintMainColor,
                        )
                        restore()
                    }
                }

                // Speed tic labels
                val maxPreferredUnits = majorTickCount * preferredUnitsPerTick
                val rangeOfPreferredUnits = 0..maxPreferredUnits
                val majorTicMarkIterable = (rangeOfPreferredUnits step preferredUnitsPerTick)
                val characterWidth = 10f
                val characterHeight = 12f
                for (speedInPreferredUnit in majorTicMarkIterable) {
                    val speedPercentage = (speedInPreferredUnit / speedRatio / maxSpeed)
                        .coerceAtMost(1f)

                    val comSpeDeg = lerp(0f, arcDegreeRange, speedPercentage)
                    val xOffsetBase = characterWidth * speedInPreferredUnit.toString().length / 2f
                    val textPosition = getPointOnCircle(
                        center,
                        centerArcSize.width * 0.84f,
                        ((startStepAngle - 180 + comSpeDeg) * PI / 180f).toFloat(),
                    )
                    val leftPercentage = speedPercentage * 2f
                    val rightPercentage = (speedPercentage - 0.5f) * 2f
                    val xOffset = when {
                        speedPercentage < 0.5f -> lerp(0f, xOffsetBase, leftPercentage)
                        speedPercentage > 0.5f -> lerp(-xOffsetBase, 0f, rightPercentage)
                        else -> 0f
                    }
                    val yOffset = when {
                        speedPercentage < 0.5f -> lerp(characterHeight, 0f, leftPercentage)
                        speedPercentage > 0.5f -> lerp(0f, characterHeight, rightPercentage)
                        else -> 0f
                    }
                    val tickLabelAlignment = when {
                        speedPercentage < 0.5f -> android.graphics.Paint.Align.RIGHT
                        speedPercentage > 0.5f -> android.graphics.Paint.Align.LEFT
                        else -> android.graphics.Paint.Align.CENTER
                    }
                    val tickLabelPaint = Paint().asFrameworkPaint().apply {
                        color = pointerColor.toArgb()
                        textSize = tickBaseLength * 0.3f
                        textAlign = tickLabelAlignment
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
