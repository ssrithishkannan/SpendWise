package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun BudgetPaceGauge(
    monthlySpent: Double,
    monthlyBudget: Double,
    modifier: Modifier = Modifier
) {
    val progress = remember(monthlySpent, monthlyBudget) {
        if (monthlyBudget <= 0) 0f
        else (monthlySpent / monthlyBudget).toFloat().coerceIn(0f, 1.5f)
    }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val cal = Calendar.getInstance()
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthTimeProgress = (currentDay.toFloat() / maxDays.toFloat())

    val isOverBudget = monthlySpent > monthlyBudget
    val isPaceFast = progress > (monthTimeProgress + 0.15f)

    val gaugeColor = when {
        isOverBudget -> Color(0xFFEF4444)
        isPaceFast -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val statusText = when {
        isOverBudget -> "Over Budget Limit"
        isPaceFast -> "Pace Above Average"
        else -> "Healthy Spending Pace"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_pace_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Gauge Canvas
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Track Arc (240 degrees from 150 to 390)
                    drawArc(
                        color = Color.DarkGray.copy(alpha = 0.2f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Arc
                    val sweep = (animatedProgress.value.coerceAtMost(1.0f) * 240f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF10B981), Color(0xFFF59E0B), gaugeColor)
                        ),
                        startAngle = 150f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = gaugeColor
                    )
                    Text(
                        text = "Used",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text telemetry
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOverBudget || isPaceFast) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = gaugeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = gaugeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${"%.2f".format(monthlySpent)} of $${"%.2f".format(monthlyBudget)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Day $currentDay of $maxDays • $${"%.2f".format((monthlyBudget - monthlySpent).coerceAtLeast(0.0))} safe margin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
