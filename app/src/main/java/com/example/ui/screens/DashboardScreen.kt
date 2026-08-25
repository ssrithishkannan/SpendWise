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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
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
import com.example.data.local.UserProfileEntity
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
import java.util.Calendar
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = (profile?.universityName ?: "CAMPUS LIFE").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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

        // 3. College Student Daily Safe-to-Spend & Meal Swipes Card
        item {
            CollegeSafeSpendAndMealPlanCard(
                profile = profile,
                monthlySpent = overview.monthlyExpense,
                monthlyLimit = overview.monthlyBudgetLimit,
                onUseMealSwipe = { viewModel.useMealSwipe() },
                onSplitBillClick = onNavigateToReconciliation
            )
        }

        // 4. Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Expense Button
                Surface(
                    onClick = onOpenAddTransaction,
                    shape = RoundedCornerShape(18.dp),
                    color = PolishSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
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

                // Split Bill (Venmo) Button
                Surface(
                    onClick = onNavigateToReconciliation,
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFE8DEF8),
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .testTag("action_split_roommate_bill")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "SPLIT BILL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFF381E72),
                            fontSize = 11.sp
                        )
                    }
                }

                // Bank Sync / Wallet Button
                Surface(
                    onClick = onNavigateToReconciliation,
                    shape = RoundedCornerShape(18.dp),
                    color = PolishPrimaryLight,
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
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

@Composable
fun CollegeSafeSpendAndMealPlanCard(
    profile: UserProfileEntity?,
    monthlySpent: Double,
    monthlyLimit: Double,
    onUseMealSwipe: () -> Unit,
    onSplitBillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
    val daysRemaining = (daysInMonth - currentDay + 1).coerceAtLeast(1)
    val budgetRemaining = (monthlyLimit - monthlySpent).coerceAtLeast(0.0)
    val safeDailySpend = budgetRemaining / daysRemaining

    val spentRatio = if (monthlyLimit > 0) (monthlySpent / monthlyLimit).toFloat() else 0f
    val (statusLabel, statusColor, statusEmoji) = when {
        spentRatio < 0.65f -> Triple("Healthy Pace", Color(0xFF10B981), "🟢")
        spentRatio < 0.90f -> Triple("Thrifty Mode", Color(0xFFF59E0B), "🟡")
        else -> Triple("Ramen Mode Alert", Color(0xFFEF4444), "🍜")
    }

    val swipesRemaining = profile?.diningHallSwipesRemaining ?: 94
    val flexDollars = profile?.flexDiningDollarsRemaining ?: 142.50
    val dormLocation = profile?.campusHousing ?: "Campus Dorm"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("college_safe_spend_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Broke-o-meter badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Campus Burn Rate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$dormLocation • $daysRemaining days left in month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusEmoji,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            // Daily Safe-To-Spend Big Value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SAFE DAILY SPEND",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", safeDailySpend)}/day",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MONTHLY LEFTOVER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", budgetRemaining)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (budgetRemaining > 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            // Dining Swipes & Flex Points Tracker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dining Swipes Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFECE8),
                    border = BorderStroke(1.dp, Color(0xFFFFD4CA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MEAL SWIPES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC83A22),
                                fontSize = 9.sp
                            )
                            Text(
                                text = "$swipesRemaining left",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF9E2310)
                            )
                        }
                        Surface(
                            onClick = onUseMealSwipe,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC83A22)
                        ) {
                            Text(
                                text = "-1",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Flex Dollars Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE6F4EA),
                    border = BorderStroke(1.dp, Color(0xFFCEEAD6))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "FLEX DINING $",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF137333),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", flexDollars)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0D5224)
                        )
                    }
                }
            }
        }
    }
}

