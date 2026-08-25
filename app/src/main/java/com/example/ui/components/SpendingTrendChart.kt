package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DaySpendingPoint(
    val dayLabel: String, // e.g. "Mon", "Tue", "18th"
    val amount: Double
)

@Composable
fun SpendingTrendChart(
    points: List<DaySpendingPoint>,
    dailyAverageBudget: Double,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val maxAmount = remember(points, dailyAverageBudget) {
        val maxFromPoints = points.maxOfOrNull { it.amount } ?: 100.0
        maxOf(maxFromPoints, dailyAverageBudget * 1.3, 50.0)
    }

    val selectedPoint = selectedPointIndex?.let { points.getOrNull(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_trend_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Velocity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedPoint != null) "${selectedPoint.dayLabel}: $${"%.2f".format(selectedPoint.amount)}"
                        else "Daily spend vs. Daily target ($${"%.0f".format(dailyAverageBudget)}/day)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedPoint != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selectedPoint != null) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "Touch to inspect",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gathering trend telemetry...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val lineColor = MaterialTheme.colorScheme.primary
                    val gradientStart = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    val gradientEnd = MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                    val targetLineColor = Color(0xFFF59E0B)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                    val nearestIndex = ((offset.x + (stepX / 2f)) / stepX).toInt().coerceIn(0, points.size - 1)
                                    selectedPointIndex = if (selectedPointIndex == nearestIndex) null else nearestIndex
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height - 24.dp.toPx()
                        val bottomY = height
                        val stepX = width / (points.size - 1).coerceAtLeast(1)

                        // 1. Draw Target Budget Pace Horizontal Dotted Line
                        val targetY = bottomY - ((dailyAverageBudget / maxAmount).toFloat() * height)
                        drawLine(
                            color = targetLineColor.copy(alpha = 0.7f),
                            start = Offset(0f, targetY),
                            end = Offset(width, targetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        // 2. Build Bezier curve path
                        val curvePath = Path()
                        val fillPath = Path()

                        val pointCoordinates = points.mapIndexed { index, point ->
                            val x = index * stepX
                            val animatedAmount = point.amount * animationProgress.value
                            val y = bottomY - ((animatedAmount / maxAmount).toFloat() * (height - 16.dp.toPx()))
                            Offset(x, y.coerceIn(8.dp.toPx(), bottomY))
                        }

                        if (pointCoordinates.isNotEmpty()) {
                            curvePath.moveTo(pointCoordinates[0].x, pointCoordinates[0].y)
                            fillPath.moveTo(pointCoordinates[0].x, bottomY)
                            fillPath.lineTo(pointCoordinates[0].x, pointCoordinates[0].y)

                            for (i in 0 until pointCoordinates.size - 1) {
                                val current = pointCoordinates[i]
                                val next = pointCoordinates[i + 1]
                                val controlPoint1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
                                val controlPoint2 = Offset(current.x + (next.x - current.x) / 2f, next.y)

                                curvePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, next.x, next.y)
                                fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, next.x, next.y)
                            }

                            fillPath.lineTo(pointCoordinates.last().x, bottomY)
                            fillPath.close()

                            // Draw Gradient Area under curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(gradientStart, gradientEnd),
                                    startY = 0f,
                                    endY = bottomY
                                )
                            )

                            // Draw Trend Line
                            drawPath(
                                path = curvePath,
                                color = lineColor,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw point dots
                            pointCoordinates.forEachIndexed { idx, point ->
                                val isSelected = selectedPointIndex == idx
                                val radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx()

                                drawCircle(
                                    color = if (isSelected) Color.White else lineColor,
                                    radius = radius,
                                    center = point
                                )
                                if (isSelected) {
                                    drawCircle(
                                        color = lineColor,
                                        radius = 8.dp.toPx(),
                                        center = point,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }

                // X-Axis Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEachIndexed { idx, pt ->
                        val isSelected = selectedPointIndex == idx
                        Text(
                            text = pt.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
