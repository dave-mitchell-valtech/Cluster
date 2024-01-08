package com.example.cluster.ui

import android.graphics.PointF
import android.util.SizeF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.util.lerp
import com.example.cluster.utilities.SpeedometerCalculator
import kotlin.math.PI

private const val CHARACTER_WIDTH = 10f
private const val CHARACTER_HEIGHT = 12f
private val calculator = SpeedometerCalculator()

@Composable
fun DialSpeedTickLabels(
    maxSpeedInPreferredUnit: Int,
    arcDegreeRange: Float,
    preferredUnitsPerMajorTick: Int,
    foregroundColor: Color,
) {
    val startStepAngle = -((arcDegreeRange - 180) / 2f).toInt()

    Canvas(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onDraw = {
            // Sizes and positions
            val center = PointF(drawContext.size.width / 2f, drawContext.size.height / 2f)
            val centerOffset = drawContext.size.center
            val centerArcSize = drawContext.size / 2f
            val tickBaseLength = centerOffset.x / 2.8f
            val tickLabelRadius = centerArcSize.width * 0.84f

            drawIntoCanvas { canvas ->
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
                        color = foregroundColor.toArgb()
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
