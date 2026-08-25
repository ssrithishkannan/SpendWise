package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CategorySlice(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Float,
    val startAngle: Float,
    val sweepAngle: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveDonutChart(
    categorySpending: Map<TransactionCategory, Double>,
    totalExpense: Double,
    modifier: Modifier = Modifier,
    onCategorySelected: (TransactionCategory?) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(categorySpending) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val slices = remember(categorySpending, totalExpense) {
        if (totalExpense <= 0) emptyList()
        else {
            var currentAngle = -90f
            categorySpending.entries
                .filter { it.value > 0 }
                .sortedByDescending { it.value }
                .map { (cat, amt) ->
                    val sweep = ((amt / totalExpense) * 360f).toFloat()
                    val slice = CategorySlice(
                        category = cat,
                        amount = amt,
                        percentage = ((amt / totalExpense) * 100f).toFloat(),
                        startAngle = currentAngle,
                        sweepAngle = sweep
                    )
                    currentAngle += sweep
                    slice
                }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("donut_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (selectedCategory != null) {
                    Surface(
                        onClick = {
                            selectedCategory = null
                            onCategorySelected(null)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Reset Filter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (slices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded yet.\nTap + to add your first!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Box(
                    modifier = Modifier.size(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(210.dp)
                            .pointerInput(slices) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = tapOffset.x - center.x
                                    val dy = tapOffset.y - center.y
                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                    val radius = size.width / 2f
                                    val innerRadius = radius - 36.dp.toPx()

                                    if (distance in innerRadius..radius) {
                                        var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (touchAngle < -90f) touchAngle += 360f

                                        val tappedSlice = slices.find { slice ->
                                            val endAngle = slice.startAngle + slice.sweepAngle
                                            if (touchAngle >= slice.startAngle && touchAngle <= endAngle) true
                                            else if (slice.startAngle + slice.sweepAngle > 270f && touchAngle <= (slice.startAngle + slice.sweepAngle - 360f)) true
                                            else false
                                        }

                                        if (tappedSlice != null) {
                                            selectedCategory = if (selectedCategory == tappedSlice.category) null else tappedSlice.category
                                            onCategorySelected(selectedCategory)
                                        }
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 32.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2f
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)

                        // Draw background track
                        drawArc(
                            color = Color.DarkGray.copy(alpha = 0.15f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // Draw animated category slices
                        slices.forEach { slice ->
                            val isSelected = selectedCategory == slice.category
                            val extraWidth = if (isSelected) 8.dp.toPx() else 0f
                            val animatedSweep = slice.sweepAngle * animationProgress.value

                            drawArc(
                                color = slice.category.color,
                                startAngle = slice.startAngle,
                                sweepAngle = animatedSweep,
                                useCenter = false,
                                topLeft = Offset(topLeft.x - extraWidth / 2f, topLeft.y - extraWidth / 2f),
                                size = Size(arcSize.width + extraWidth, arcSize.height + extraWidth),
                                style = Stroke(width = strokeWidth + extraWidth, cap = StrokeCap.Butt)
                            )
                        }
                    }

                    // Center information text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val displayCat = selectedCategory?.let { sel ->
                            slices.find { it.category == sel }
                        }

                        if (displayCat != null) {
                            Text(
                                text = displayCat.category.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = displayCat.category.color,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "$${"%.2f".format(displayCat.amount)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${"%.1f".format(displayCat.percentage)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$${"%.2f".format(totalExpense)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${slices.size} Categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Category Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.take(6).forEach { slice ->
                        val isSelected = selectedCategory == slice.category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = if (isSelected) null else slice.category
                                onCategorySelected(selectedCategory)
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(8.dp),
                                        shape = CircleShape,
                                        color = slice.category.color
                                    ) {}
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${slice.category.displayName} (${slice.percentage.toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = slice.category.color.copy(alpha = 0.2f),
                                selectedLabelColor = slice.category.color
                            )
                        )
                    }
                }
            }
        }
    }
}
