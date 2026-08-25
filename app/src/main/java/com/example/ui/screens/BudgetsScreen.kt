package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CategoryBudgetEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.ui.components.WeeklyHabitPills
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryDark
import com.example.ui.theme.PolishStreakRed
import com.example.ui.theme.PolishTertiaryGreen
import com.example.ui.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetsScreen(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val streak by viewModel.userStreak.collectAsStateWithLifecycle()
    val overview by viewModel.overviewState.collectAsStateWithLifecycle()

    var editingBudget by remember { mutableStateOf<CategoryBudgetEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val (monthStart, monthEnd) = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(java.util.Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        Pair(start, end)
    }

    val currentMonthExpenses = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE && it.timestamp in monthStart..monthEnd }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budgets_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Budgets & Habit Streaks",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Control monthly spending caps and preserve daily budgeting streaks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Habit Streaks & Discipline Mastery Card
        item {
            val streakDays = streak?.currentStreak ?: 0
            val totalLogged = streak?.totalDaysLogged ?: 0
            val longest = streak?.longestStreak ?: 0
            val freezes = streak?.streakFreezeCount ?: 2
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val isLoggedToday = streak?.lastLoggedDate == todayStr

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_discipline_card"),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PolishPrimary
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(PolishPrimary, PolishPrimaryDark)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = PolishStreakRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Consistent Budgeting Streak",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AcUnit,
                                        contentDescription = null,
                                        tint = Color(0xFFE0F2FE),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$freezes Freeze Shields",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Current Streak", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("$streakDays Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = PolishTertiaryGreen)
                            }
                            Column {
                                Text("Longest Streak", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("$longest Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                            }
                            Column {
                                Text("Total Logged", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("$totalLogged Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD1E4FF))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        WeeklyHabitPills(streakDays = streakDays, isLoggedToday = isLoggedToday)
                    }
                }
            }
        }

        // 3. Push Notification Spending Alerts Showcase Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("push_notification_settings_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Monthly Spending Push Alerts",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Notifies when reaching 80% and 100% of limits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.triggerTestBudgetAlertNotification(TransactionCategory.FOOD_DINING)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_push_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trigger Sample Spending Limit Notification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 4. Monthly Category Budgets Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Spending Limits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Cap: $${"%,.0f".format(overview.monthlyBudgetLimit)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. Category Budget Items
        items(TransactionCategory.entries.filter { it != TransactionCategory.STUDENT_AID_ALLOWANCE && it != TransactionCategory.CAMPUS_JOB_SALARY }) { cat ->
            val catBudget = budgets.find { it.category == cat.name }
            val limit = catBudget?.monthlyLimit ?: cat.defaultMonthlyBudget
            val spent = currentMonthExpenses.filter { it.category == cat }.sumOf { it.amount }
            val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
            val percentUsed = if (limit > 0) ((spent / limit) * 100).toInt() else 0
            val isAlertEnabled = catBudget?.isAlertEnabled ?: true

            CategoryBudgetRowCard(
                category = cat,
                spent = spent,
                limit = limit,
                progress = progress,
                percentUsed = percentUsed,
                isAlertEnabled = isAlertEnabled,
                onEditClick = {
                    editingBudget = catBudget ?: CategoryBudgetEntity(
                        category = cat.name,
                        monthlyLimit = limit,
                        alertThresholdPercent = 80,
                        isAlertEnabled = true
                    )
                    showEditDialog = true
                }
            )
        }
    }

    // Edit Budget Limit Dialog
    if (showEditDialog && editingBudget != null) {
        val b = editingBudget!!
        val cat = TransactionCategory.fromString(b.category)
        var limitInput by remember { mutableStateOf(b.monthlyLimit.toInt().toString()) }
        var thresholdValue by remember { mutableStateOf(b.alertThresholdPercent.toFloat()) }
        var alertsEnabled by remember { mutableStateOf(b.isAlertEnabled) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adjust ${cat.displayName} Limit")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Monthly Spending Cap ($)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_limit_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Push Alert at ${thresholdValue.toInt()}% Spend",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = alertsEnabled,
                            onCheckedChange = { alertsEnabled = it }
                        )
                    }

                    if (alertsEnabled) {
                        Slider(
                            value = thresholdValue,
                            onValueChange = { thresholdValue = it },
                            valueRange = 50f..100f,
                            steps = 9
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = limitInput.toDoubleOrNull() ?: 100.0
                        viewModel.updateBudgetLimit(
                            category = cat,
                            newLimit = parsed,
                            threshold = thresholdValue.toInt(),
                            alertEnabled = alertsEnabled
                        )
                        showEditDialog = false
                    }
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryBudgetRowCard(
    category: TransactionCategory,
    spent: Double,
    limit: Double,
    progress: Float,
    percentUsed: Int,
    isAlertEnabled: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverBudget = spent > limit
    val statusColor = when {
        isOverBudget -> Color(0xFFEF4444)
        percentUsed >= 80 -> Color(0xFFF59E0B)
        else -> category.color
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_budget_card_${category.name}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.displayName,
                            tint = category.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${"%.2f".format(spent)} spent of $${"%.0f".format(limit)} limit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "$percentUsed%",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Edit Limit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val remaining = limit - spent
                Text(
                    text = if (remaining >= 0) "$${"%.2f".format(remaining)} remaining" else "$${"%.2f".format(-remaining)} over limit",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (remaining >= 0) MaterialTheme.colorScheme.outline else Color(0xFFEF4444),
                    fontWeight = FontWeight.SemiBold
                )

                if (isAlertEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Push Alert On",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
