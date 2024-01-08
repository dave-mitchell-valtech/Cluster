package com.example.cluster.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.rotate
import com.example.cluster.utilities.SpeedometerCalculator

private val calculator = SpeedometerCalculator()

@Composable
fun DialSpeedTicks(
    mainColor: MutableState<Color>,
    arcDegreeRange: Float,
    tickCount: Int,
    minorTicksPerMajorTick: Int = 4,
) {
    val mainPaint = Paint().apply { color = mainColor.value }
    val degreesMarkerStep = arcDegreeRange / tickCount
    val startStepAngle = -((arcDegreeRange - 180) / 2f).toInt()

    Canvas(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onDraw = {
            // Sizes and positions
            val centerOffset = drawContext.size.center
            val tickBaseLength = centerOffset.x / 2.8f

            drawIntoCanvas { canvas ->
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
            }
        },
    )
}
