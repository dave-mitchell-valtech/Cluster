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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Speedometer(
    maxSpeed: Float = 80f,
    arcDegreeRange: Int = 270,
    numberOfMarkers: Int = 40,
    clusterViewModel: ClusterViewModel = viewModel(),
) {
    // Static properties
    val wrapDegrees = (arcDegreeRange - 180) / 2f
    val startArcAngle = 180 - wrapDegrees
    val startStepAngle = -wrapDegrees.toInt()
    val degreesMarkerStep = arcDegreeRange / numberOfMarkers

    // Representations of speed
    val currentSpeed by remember { clusterViewModel.currentSpeed }
    val absoluteSpeed = abs(currentSpeed)
    val interpolatedAbsoluteSpeed by animateFloatAsState(
        label = "speed",
        targetValue = absoluteSpeed,
        animationSpec = spring(
            dampingRatio = 1f,
            stiffness = 200f,
        ),
    )
    val composeSpeedDegree = lerp(
        0f,
        arcDegreeRange.toFloat(),
        (interpolatedAbsoluteSpeed / maxSpeed).coerceAtMost(1f),
    )

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
        else -> 1f to longLength * .8f
    }.let {
        paintMainColor.strokeWidth = it.first
        it.second to longLength
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onDraw = {
            // Sizes and positions
            val w = drawContext.size.width
            val h = drawContext.size.height
            val centerOffset = Offset(w / 2f, h / 2f)
            val quarterOffset = Offset(w / 4f, h / 4f)
            val centerArcSize = Size(w / 2f, h / 2f)
            val centerArcStroke = Stroke(20f, 0f, StrokeCap.Butt)
            val center = PointF(w / 2f, h / 2f)
            val degrees = ((startStepAngle - 180 + composeSpeedDegree) * PI / 180f).toFloat()
            val firstPoint = getPointOnCircle(center, 5f, degrees - 90f)
            val secondPoint = getPointOnCircle(center, 5f, degrees + 90f)
            val farPoint = getPointOnCircle(center, (centerArcSize.width / 2f), degrees)

            drawIntoCanvas { canvas ->
                // Available progress range
                drawArc(
                    secondaryColor,
                    startArcAngle,
                    arcDegreeRange.toFloat(),
                    false,
                    topLeft = quarterOffset,
                    size = centerArcSize,
                    style = centerArcStroke,
                )

                // Progress
                drawArc(
                    mainColor,
                    startArcAngle,
                    composeSpeedDegree,
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
                val arcRange = startStepAngle..(startStepAngle + arcDegreeRange)
                val ticMarkIterable = (arcRange step degreesMarkerStep).withIndex()
                for ((counter, rotation) in ticMarkIterable) {
                    val (lineStartX, lineEndX) = getLineEndsX(70f, counter)
                    canvas.run {
                        save()
                        rotate(rotation.toFloat(), w / 2f, h / 2f)
                        drawLine(
                            Offset(lineStartX, h / 2f),
                            Offset(lineEndX, h / 2f),
                            paintMainColor,
                        )
                        restore()
                    }
                }

                // Speed tic labels
                val majorTicMarkIterable = (0..160 step 20)
                for (speedInPreferredUnit in majorTicMarkIterable) {
                    val speedInMetersPerSecond = speedInPreferredUnit / 3.6f // TODO: Do not assume kmph is preferred
                    val comSpeDeg = lerp(
                        0f,
                        arcDegreeRange.toFloat(),
                        (speedInMetersPerSecond / maxSpeed).coerceAtMost(1f),
                    )
                    val deg = ((startStepAngle - 180 + comSpeDeg) * PI / 180f).toFloat()
                    val textPosition = getPointOnCircle(center, centerArcSize.width * .9f, deg)
                    canvas.nativeCanvas.drawText(
                        "$speedInPreferredUnit",
                        textPosition.x,
                        textPosition.y,
                        Paint().asFrameworkPaint().apply {
                            this.color = pointerColor.toArgb()
                        },
                    )
                }
            }
        },
    )
}
