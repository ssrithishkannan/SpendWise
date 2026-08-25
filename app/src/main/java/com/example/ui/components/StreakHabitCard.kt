package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StreakEntity
import com.example.ui.theme.PolishMilestoneGold
import com.example.ui.theme.PolishMilestoneOrange
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StreakHabitCard(
    streak: StreakEntity?,
    modifier: Modifier = Modifier,
    onStreakDetailsClick: () -> Unit = {}
) {
    val streakDays = streak?.currentStreak ?: 0
    val totalXp = streak?.totalXp ?: 0
    val level = streak?.currentLevel ?: 1

    val xpInCurrentLevel = totalXp % 250
    val xpProgress = (xpInCurrentLevel / 250f).coerceIn(0f, 1f)
    val progressPercentage = (xpProgress * 100).toInt().coerceAtLeast(15)

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val isLoggedToday = streak?.lastLoggedDate == todayStr

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_habit_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Weekly Activity + Badge Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = PolishPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    onClick = onStreakDetailsClick,
                    shape = RoundedCornerShape(8.dp),
                    color = PolishPrimaryLight
                ) {
                    Text(
                        text = if (streakDays >= 7) "Budgeting Champ" else "Level $level Habit",
                        style = MaterialTheme.typography.labelSmall,
                        color = PolishPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7-Day Vertical Activity Bars
            WeeklyActivityBars(streakDays = streakDays, isLoggedToday = isLoggedToday)

            Spacer(modifier = Modifier.height(16.dp))

            // Next Milestone Reward Preview Card
            Surface(
                onClick = onStreakDetailsClick,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Gold Gradient Circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PolishMilestoneGold,
                                            PolishMilestoneOrange
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Milestone Reward",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Next Milestone: Level ${level + 1} Pro",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Log $streakDays consecutive days for bonus XP!",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyActivityBars(streakDays: Int, isLoggedToday: Boolean) {
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    val heights = listOf(0.40f, 0.65f, 0.90f, 0.35f, 0.55f, 0.45f, 0.25f)
    val cal = Calendar.getInstance()
    val todayDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val isToday = index == todayDayOfWeek
            val isPastOrToday = index <= todayDayOfWeek
            val isActive = if (isToday) isLoggedToday else (isPastOrToday && (todayDayOfWeek - index) < streakDays)

            val barHeightFraction = if (isToday) 0.88f else heights.getOrElse(index) { 0.5f }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Vertical Bar with rounded top
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .fillMaxHeight(barHeightFraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (isToday || isActive) PolishPrimary else PolishPrimaryLight
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) PolishPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun WeeklyHabitPills(streakDays: Int, isLoggedToday: Boolean) {
    WeeklyActivityBars(streakDays = streakDays, isLoggedToday = isLoggedToday)
}
