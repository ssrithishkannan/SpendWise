package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfettiCelebration
import com.example.ui.screens.AddTransactionBottomSheet
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingSetupScreen
import com.example.ui.screens.ReconciliationScreen
import com.example.ui.screens.RewardsAndAiScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.SpendWiseTheme
import com.example.ui.viewmodel.FinancialViewModel

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    LEDGER("Ledger", Icons.Default.ReceiptLong, "nav_ledger"),
    BUDGETS("Budgets", Icons.Default.PieChart, "nav_budgets"),
    RECONCILIATION("Bank Sync", Icons.Default.AccountBalance, "nav_reconciliation"),
    REWARDS("Rewards & AI", Icons.Default.EmojiEvents, "nav_rewards")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FinancialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SpendWiseTheme {
                val context = LocalContext.current
                var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
                var showAddSheet by remember { mutableStateOf(false) }
                var showProfileScreen by remember { mutableStateOf(false) }

                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val showCelebration by viewModel.showCelebration.collectAsStateWithLifecycle()

                // Request Notification Permission on Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* Handled */ }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val isOnboardingDone = userProfile?.isOnboardingCompleted ?: true

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isOnboardingDone && userProfile != null) {
                        // Essential Onboarding Setup Flow
                        OnboardingSetupScreen(
                            currentProfile = userProfile,
                            onComplete = { completedProfile ->
                                viewModel.completeOnboarding(completedProfile)
                            }
                        )
                    } else if (showProfileScreen) {
                        // User Profile & Settings Screen
                        UserProfileScreen(
                            viewModel = viewModel,
                            onBackClick = { showProfileScreen = false },
                            onReRunSetup = {
                                showProfileScreen = false
                                viewModel.resetOnboarding()
                            }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                Surface(
                                    tonalElevation = 3.dp,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                ) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        tonalElevation = 0.dp,
                                        modifier = Modifier.testTag("bottom_navigation_bar")
                                    ) {
                                        NavigationTab.values().forEach { tab ->
                                            val isSelected = currentTab == tab
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = { currentTab = tab },
                                                icon = {
                                                    Icon(
                                                        imageVector = tab.icon,
                                                        contentDescription = tab.title,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = tab.title,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                ),
                                                modifier = Modifier.testTag(tab.tag)
                                            )
                                        }
                                    }
                                }
                            },
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { showAddSheet = true },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                                    modifier = Modifier.testTag("fab_add_transaction")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Log Expense or Income",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentTab) {
                                    NavigationTab.DASHBOARD -> DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToTransactions = { currentTab = NavigationTab.LEDGER },
                                        onNavigateToBudgets = { currentTab = NavigationTab.BUDGETS },
                                        onNavigateToReconciliation = { currentTab = NavigationTab.RECONCILIATION },
                                        onNavigateToRewards = { currentTab = NavigationTab.REWARDS },
                                        onOpenAddTransaction = { showAddSheet = true },
                                        onOpenProfile = { showProfileScreen = true }
                                    )
                                    NavigationTab.LEDGER -> TransactionsScreen(
                                        viewModel = viewModel,
                                        onOpenAddTransaction = { showAddSheet = true }
                                    )
                                    NavigationTab.BUDGETS -> BudgetsScreen(
                                        viewModel = viewModel
                                    )
                                    NavigationTab.RECONCILIATION -> ReconciliationScreen(
                                        viewModel = viewModel
                                    )
                                    NavigationTab.REWARDS -> RewardsAndAiScreen(
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }

                        // Add Transaction Bottom Sheet
                        if (showAddSheet) {
                            AddTransactionBottomSheet(
                                viewModel = viewModel,
                                onDismiss = { showAddSheet = false }
                            )
                        }
                    }

                    // Confetti Celebration Overlay for Milestones and Habit Streak Rewards
                    ConfettiCelebration(
                        isActive = showCelebration,
                        onFinished = { viewModel.dismissCelebration() }
                    )
                }
            }
        }
    }
}
