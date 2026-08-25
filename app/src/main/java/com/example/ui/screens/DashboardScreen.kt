package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.ui.components.BudgetPaceGauge
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.SpendingTrendChart
import com.example.ui.components.StreakHabitCard
import com.example.ui.theme.PolishGreenSuccess
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryDark
import com.example.ui.theme.PolishPrimaryLight
import com.example.ui.theme.PolishSecondary
import com.example.ui.theme.PolishStreakBg
import com.example.ui.theme.PolishStreakBorder
import com.example.ui.theme.PolishStreakRed
import com.example.ui.theme.PolishTertiaryGreen
import com.example.ui.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FinancialViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToReconciliation: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val overview by viewModel.overviewState.collectAsStateWithLifecycle()
    val streak by viewModel.userStreak.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val syncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var selectedChartCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    val streakDays = streak?.currentStreak ?: 0
    val firstName = profile?.userName?.trim()?.split(" ")?.firstOrNull()?.ifBlank { "Jordan" } ?: "Jordan"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Professional Polish Header with Welcome, Streak Flame Pill & Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "Hey, $firstName",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Streak Pill
                    Surface(
                        onClick = onNavigateToRewards,
                        shape = RoundedCornerShape(50),
                        color = PolishStreakBg,
                        border = BorderStroke(1.dp, PolishStreakBorder),
                        modifier = Modifier.testTag("dashboard_streak_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$streakDays",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishStreakRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Active Streak",
                                tint = PolishStreakRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Avatar / Profile Pill
                    Surface(
                        shape = CircleShape,
                        color = PolishPrimary,
                        border = BorderStroke(2.dp, Color.White),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onOpenProfile() }
                            .testTag("dashboard_profile_avatar_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (syncStatus.isSyncing) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Syncing",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = firstName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Professional Polish Hero Balance Card
        item {
            ProfessionalPolishHeroBalanceCard(
                totalBalance = overview.totalBalance,
                monthlyIncome = overview.monthlyIncome,
                monthlyExpense = overview.monthlyExpense,
                monthlyLimit = overview.monthlyBudgetLimit,
                savingsRate = overview.netSavingsRate,
                isSyncing = syncStatus.isSyncing,
                onReconciliationClick = onNavigateToReconciliation
            )
        }

        // 3. Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Expense Button
                Surface(
                    onClick = onOpenAddTransaction,
                    shape = RoundedCornerShape(20.dp),
                    color = PolishSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .testTag("action_add_expense")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color(0xFF1D192B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ADD EXPENSE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFF1D192B),
                            fontSize = 11.sp
                        )
                    }
                }

                // Bank Sync / Wallet Button
                Surface(
                    onClick = onNavigateToReconciliation,
                    shape = RoundedCornerShape(20.dp),
                    color = PolishPrimaryLight,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .testTag("action_wallet_sync")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF001D35),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "WALLET & SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFF001D35),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 4. Habit Streaks Card (Gamified daily discipline)
        item {
            StreakHabitCard(
                streak = streak,
                onStreakDetailsClick = onNavigateToRewards
            )
        }

        // 5. Interactive Donut Chart (Category Breakdown)
        item {
            InteractiveDonutChart(
                categorySpending = overview.categoryBreakdown,
                totalExpense = overview.monthlyExpense,
                onCategorySelected = { selectedChartCategory = it }
            )
        }

        // 6. Spending Velocity Curve (Daily spend vs target)
        item {
            SpendingTrendChart(
                points = overview.trendPoints,
                dailyAverageBudget = overview.monthlyBudgetLimit / 30.0
            )
        }

        // 7. Budget Pace Gauge
        item {
            BudgetPaceGauge(
                monthlySpent = overview.monthlyExpense,
                monthlyBudget = overview.monthlyBudgetLimit
            )
        }

        // 8. Recent Transactions Header & List Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All (${transactions.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToTransactions() }
                )
            }
        }

        val filteredRecent = if (selectedChartCategory != null) {
            transactions.filter { it.category == selectedChartCategory }
        } else {
            transactions.take(5)
        }

        if (filteredRecent.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredRecent, key = { it.id }) { tx ->
                TransactionRowCard(
                    transaction = tx,
                    onDelete = { viewModel.deleteTransaction(tx) }
                )
            }
        }
    }
}

@Composable
fun ProfessionalPolishHeroBalanceCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    monthlyLimit: Double,
    savingsRate: Float,
    isSyncing: Boolean,
    onReconciliationClick: () -> Unit
) {
    val usedRatio = if (monthlyLimit > 0) (monthlyExpense / monthlyLimit).toFloat().coerceIn(0f, 1f) else 0.72f
    val usedPercentage = (usedRatio * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("total_balance_card"),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = PolishPrimary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PolishPrimary,
                            PolishPrimaryDark
                        )
                    )
                )
                .padding(24.dp)
        ) {
            // Decorative background circle overlay in corner
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-16).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                // Top row: Total Balance + Cloud Done pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Total Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${"%,.2f".format(totalBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    }

                    Surface(
                        onClick = onReconciliationClick,
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.20f)
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                contentDescription = "Bank Sync Status",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Monthly Limit Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monthly Limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$usedPercentage% used",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(usedRatio)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(PolishTertiaryGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Real-time sync with Chase Bank active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Income vs Expenses vs Savings Rate Mini Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Income
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = PolishTertiaryGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "+$${"%,.0f".format(monthlyIncome)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishTertiaryGreen
                        )
                    }

                    // Expenses
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFFFB4AB),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Expenses",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "-$${"%,.0f".format(monthlyExpense)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB4AB)
                        )
                    }

                    // Savings Rate
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Savings Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "${(savingsRate * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowCard(
    transaction: TransactionEntity,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountPrefix = if (isExpense) "-$" else "+$"
    val amountColor = if (isExpense) MaterialTheme.colorScheme.onSurface else PolishGreenSuccess
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_row_${transaction.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
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
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.category.icon,
                        contentDescription = transaction.category.displayName,
                        tint = transaction.category.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateFormat.format(Date(transaction.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (transaction.isReconciled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PolishGreenSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Reconciled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PolishGreenSuccess,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${"%.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
                Text(
                    text = transaction.paymentMethod.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
