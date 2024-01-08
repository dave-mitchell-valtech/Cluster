package com.example.cluster.ui

import android.graphics.PointF
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.lerp
import com.example.cluster.utilities.SpeedometerCalculator
import kotlin.math.PI
import kotlin.math.absoluteValue

private val calculator = SpeedometerCalculator()

@Composable
fun DialSpeedIndication(
    speed: MutableState<Float>,
    maxSpeed: Float,
    mainColor: MutableState<Color>,
    secondaryColor: MutableState<Color>,
    foregroundColor: Color,
    arcDegreeRange: Float,
) {
    val foregroundPaint = Paint().apply { color = foregroundColor }
    val (startArcAngle, startStepAngle) = ((arcDegreeRange - 180) / 2f)
        .let { (180 - it) to -it.toInt() }
    val interpolatedAbsoluteSpeed by animateFloatAsState(
        label = "speed",
        targetValue = speed.value.absoluteValue,
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
            val centerArcStroke = Stroke(centerOffset.x / 10f, 0f, StrokeCap.Butt)
            val pointerLength = centerArcSize.width / 2f
            val pointerFirstPoint = calculator.getPointOnCircle(center, 5f, degrees - 90f)
            val pointerSecondPoint = calculator.getPointOnCircle(center, 5f, degrees + 90f)
            val pointerFarPoint = calculator.getPointOnCircle(center, pointerLength, degrees)

            // Available progress range
            drawArc(
                secondaryColor.value,
                startArcAngle,
                arcDegreeRange,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke,
            )

            // Progress
            drawArc(
                mainColor.value,
                startArcAngle,
                interpolatedAbsoluteSpeedDegree,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke,
            )

            // Pointer
            drawCircle(foregroundPaint.color, 14f, centerOffset)
            drawPath(
                Path().apply {
                    moveTo(pointerFirstPoint.x, pointerFirstPoint.y)
                    lineTo(pointerSecondPoint.x, pointerSecondPoint.y)
                    lineTo(pointerFarPoint.x, pointerFarPoint.y)
                    lineTo(pointerFirstPoint.x, pointerFirstPoint.y)
                    close()
                },
                foregroundPaint.color,
            )
        },
    )
}
