package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val paymentMethod: PaymentMethod,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val isReconciled: Boolean = false,
    val bankTransactionRef: String? = null,
    val isCloudSynced: Boolean = true
)

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey
    val category: String, // TransactionCategory name
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 80, // e.g., 80% warning
    val isAlertEnabled: Boolean = true
)

@Entity(tableName = "user_streak")
data class StreakEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoggedDate: String = "", // Format: "YYYY-MM-DD"
    val streakFreezeCount: Int = 2,
    val totalDaysLogged: Int = 0,
    val totalXp: Int = 0,
    val currentLevel: Int = 1
)

@Entity(tableName = "bank_accounts")
data class BankSyncAccountEntity(
    @PrimaryKey
    val accountId: String,
    val institutionName: String,
    val accountNumberMask: String, // e.g. "•••• 4892"
    val accountType: String, // "Checking", "Savings", "Credit Card"
    val balance: Double,
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val isAutoSyncActive: Boolean = true,
    val pendingDiscrepanciesCount: Int = 0
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val isOnboardingCompleted: Boolean = false,
    val userName: String = "Jordan Walker",
    val userEmail: String = "jordan.aura@gmail.com",
    val avatarIndex: Int = 0,
    val selectedLanguage: String = "English",
    val selectedCurrencySymbol: String = "$",
    val selectedCurrencyCode: String = "USD",
    val isGoogleCloudSyncActive: Boolean = true,
    val isAutoExpenseCalculationActive: Boolean = true,
    val isBankSyncActive: Boolean = true,
    val primaryLinkedBank: String = "Chase Sapphire",
    val monthlyBudgetLimit: Double = 2500.0,
    val targetSavingsRatePercent: Int = 30,
    val spendingAlertsEnabled: Boolean = true,
    val weeklyReportEnabled: Boolean = true
)
