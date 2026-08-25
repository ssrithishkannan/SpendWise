package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BankSyncAccountEntity
import com.example.data.local.CategoryBudgetEntity
import com.example.data.local.FinPulseDao
import com.example.data.local.StreakEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.BadgeItem
import com.example.data.model.BadgeTier
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.remote.BankFeedTransaction
import com.example.data.remote.BankMatchStatus
import com.example.data.remote.BankReconciliationService
import com.example.data.remote.CloudSyncStatus
import com.example.data.remote.GeminiFinancialService
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinancialRepository(
    private val dao: FinPulseDao,
    val bankReconciliationService: BankReconciliationService = BankReconciliationService(),
    val geminiService: GeminiFinancialService = GeminiFinancialService()
) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allBudgets: Flow<List<CategoryBudgetEntity>> = dao.getAllBudgets()
    val userStreak: Flow<StreakEntity?> = dao.getStreak()
    val bankAccounts: Flow<List<BankSyncAccountEntity>> = dao.getAllBankAccounts()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val bankFeed: StateFlow<List<BankFeedTransaction>> = bankReconciliationService.bankFeed
    val cloudSyncStatus: StateFlow<CloudSyncStatus> = bankReconciliationService.cloudSyncState

    suspend fun updateUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateUserProfile(profile)
    }

    suspend fun completeOnboarding(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateUserProfile(profile.copy(isOnboardingCompleted = true))
    }

    suspend fun insertTransaction(transaction: TransactionEntity, context: Context?): Long = withContext(Dispatchers.IO) {
        val insertedId = dao.insertTransaction(transaction)

        // 1. Process daily habit streak
        updateStreakForNewEntry(context)

        // 2. Evaluate budget alerts
        if (transaction.type == TransactionType.EXPENSE && context != null) {
            checkCategoryBudgetAlert(transaction.category, context)
        }

        insertedId
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(transaction)
    }

    suspend fun updateBudget(budget: CategoryBudgetEntity) = withContext(Dispatchers.IO) {
        dao.updateBudget(budget)
    }

    suspend fun reconcileWithBank(localTxId: Long, bankTxId: String, bankRef: String) = withContext(Dispatchers.IO) {
        dao.setReconciliationStatus(localTxId, reconciled = true, bankRef = bankRef)
        bankReconciliationService.markTransactionMatched(bankTxId, localTxId)
    }

    suspend fun importFromBankFeed(bankTx: BankFeedTransaction, context: Context?) = withContext(Dispatchers.IO) {
        val newTx = TransactionEntity(
            title = bankTx.merchant,
            amount = bankTx.amount,
            type = TransactionType.EXPENSE,
            category = bankTx.suggestedCategory,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            timestamp = bankTx.timestamp,
            note = "Imported & Reconciled from ${bankTx.institution}",
            isReconciled = true,
            bankTransactionRef = bankTx.id,
            isCloudSynced = true
        )
        val id = dao.insertTransaction(newTx)
        bankReconciliationService.markTransactionImported(bankTx.id, id)
        if (context != null) {
            checkCategoryBudgetAlert(bankTx.suggestedCategory, context)
        }
    }

    suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        bankReconciliationService.performCloudSync()
    }

    private suspend fun updateStreakForNewEntry(context: Context?) {
        val currentStreakEntity = dao.getStreakSync() ?: StreakEntity()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        if (currentStreakEntity.lastLoggedDate == todayStr) {
            // Already logged today, just award small micro XP
            val updated = currentStreakEntity.copy(
                totalXp = currentStreakEntity.totalXp + 10,
                currentLevel = calculateLevel(currentStreakEntity.totalXp + 10)
            )
            dao.insertOrUpdateStreak(updated)
            return
        }

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

        val newStreakCount = if (currentStreakEntity.lastLoggedDate == yesterdayStr || currentStreakEntity.lastLoggedDate.isEmpty()) {
            currentStreakEntity.currentStreak + 1
        } else {
            // Missed a day: check if freeze available
            if (currentStreakEntity.streakFreezeCount > 0) {
                currentStreakEntity.currentStreak + 1
            } else {
                1
            }
        }

        val longest = maxOf(currentStreakEntity.longestStreak, newStreakCount)
        val streakXpEarned = 50 + (newStreakCount * 15)
        val newTotalXp = currentStreakEntity.totalXp + streakXpEarned
        val newDaysLogged = currentStreakEntity.totalDaysLogged + 1

        val updatedEntity = currentStreakEntity.copy(
            currentStreak = newStreakCount,
            longestStreak = longest,
            lastLoggedDate = todayStr,
            totalDaysLogged = newDaysLogged,
            totalXp = newTotalXp,
            currentLevel = calculateLevel(newTotalXp)
        )

        dao.insertOrUpdateStreak(updatedEntity)

        if (context != null && newStreakCount > 1) {
            NotificationHelper.showStreakMilestoneAlert(context, newStreakCount, streakXpEarned)
        }
    }

    private fun calculateLevel(xp: Int): Int {
        return (xp / 250) + 1
    }

    private suspend fun checkCategoryBudgetAlert(category: TransactionCategory, context: Context) {
        val budgets = dao.getAllBudgets().firstOrNull() ?: emptyList()
        val catBudget = budgets.find { it.category == category.name } ?: return

        if (!catBudget.isAlertEnabled || catBudget.monthlyLimit <= 0) return

        // Compute current month expenses for this category
        val (monthStart, monthEnd) = getCurrentMonthRange()
        val allTx = dao.getAllTransactions().firstOrNull() ?: emptyList()
        val spentInCat = allTx
            .filter { it.category == category && it.type == TransactionType.EXPENSE && it.timestamp in monthStart..monthEnd }
            .sumOf { it.amount }

        val percent = ((spentInCat / catBudget.monthlyLimit) * 100).toInt()

        if (percent >= catBudget.alertThresholdPercent) {
            NotificationHelper.showBudgetLimitAlert(
                context = context,
                categoryName = category.displayName,
                spentAmount = spentInCat,
                limitAmount = catBudget.monthlyLimit,
                percent = percent
            )
        }
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        return Pair(start, end)
    }

    fun evaluateBadges(
        transactions: List<TransactionEntity>,
        streak: StreakEntity?,
        budgets: List<CategoryBudgetEntity>
    ): List<BadgeItem> {
        val streakCount = streak?.currentStreak ?: 0
        val totalLogged = streak?.totalDaysLogged ?: transactions.size
        val reconciledCount = transactions.count { it.isReconciled }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalBudget = budgets.sumOf { it.monthlyLimit }

        return listOf(
            BadgeItem(
                id = "first_entry",
                title = "Seed of Wealth",
                description = "Logged your very first transaction in FinPulse.",
                tier = BadgeTier.BRONZE,
                xpReward = 100,
                progress = if (transactions.isNotEmpty()) 1.0f else 0.0f,
                isUnlocked = transactions.isNotEmpty(),
                unlockedDate = if (transactions.isNotEmpty()) "Unlocked" else null
            ),
            BadgeItem(
                id = "streak_3_days",
                title = "3-Day Ignition",
                description = "Build the habit: Track your expenses 3 days in a row.",
                tier = BadgeTier.BRONZE,
                xpReward = 150,
                progress = (streakCount / 3.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = streakCount >= 3,
                unlockedDate = if (streakCount >= 3) "Active Streak" else null
            ),
            BadgeItem(
                id = "streak_7_days",
                title = "Discipline Master",
                description = "Maintain a stellar 7-day expense tracking streak.",
                tier = BadgeTier.SILVER,
                xpReward = 300,
                progress = (streakCount / 7.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = streakCount >= 7,
                unlockedDate = if (streakCount >= 7) "Active Streak" else null
            ),
            BadgeItem(
                id = "streak_30_days",
                title = "Zen Money Sensei",
                description = "Master financial mindfulness with a 30-day streak.",
                tier = BadgeTier.DIAMOND,
                xpReward = 1000,
                progress = (streakCount / 30.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = streakCount >= 30
            ),
            BadgeItem(
                id = "reconciliation_ace",
                title = "Reconciliation Ace",
                description = "Match and reconcile at least 5 transactions with bank feeds.",
                tier = BadgeTier.SILVER,
                xpReward = 250,
                progress = (reconciledCount / 5.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = reconciledCount >= 5,
                unlockedDate = if (reconciledCount >= 5) "Mastered" else null
            ),
            BadgeItem(
                id = "budget_guardian",
                title = "Budget Guardian",
                description = "Stay under your monthly budget limit with total peace of mind.",
                tier = BadgeTier.GOLD,
                xpReward = 500,
                progress = if (totalBudget > 0 && totalExpenses <= totalBudget) 1.0f else 0.7f,
                isUnlocked = totalBudget > 0 && totalExpenses <= totalBudget,
                unlockedDate = if (totalBudget > 0 && totalExpenses <= totalBudget) "Current Month" else null
            ),
            BadgeItem(
                id = "cloud_architect",
                title = "Cloud Explorer",
                description = "Connect 3 live bank feeds with encrypted real-time sync.",
                tier = BadgeTier.GOLD,
                xpReward = 400,
                progress = 1.0f,
                isUnlocked = true,
                unlockedDate = "Active Cloud"
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingTx = dao.getAllTransactions().firstOrNull() ?: emptyList()
        if (existingTx.isEmpty()) {
            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // 1. Initial Sample Transactions
            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Monthly Software Engineering Salary",
                    amount = 4500.00,
                    type = TransactionType.INCOME,
                    category = TransactionCategory.SALARY,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 20),
                    note = "Direct Deposit Employer Inc.",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Whole Foods Organic Market",
                    amount = 84.50,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.GROCERIES,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    timestamp = now - (oneDay * 1),
                    note = "Produce, berries & sourdough",
                    isReconciled = true,
                    bankTransactionRef = "bank_tx_101",
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Uber Comfort to Airport",
                    amount = 38.20,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.TRANSPORT,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    timestamp = now - (oneDay * 2),
                    note = "Flight commute",
                    isReconciled = true,
                    bankTransactionRef = "bank_tx_102",
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Apartment Rent & Service Fee",
                    amount = 1250.00,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.HOUSING_BILLS,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 22),
                    note = "Monthly lease payment",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Blue Bottle Artisanal Coffee",
                    amount = 6.75,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.FOOD_DINING,
                    paymentMethod = PaymentMethod.DEBIT_CARD,
                    timestamp = now - (3600000L * 4), // 4 hours ago
                    note = "Morning espresso & pastry",
                    isReconciled = false,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "S&P 500 Index ETF Auto-Invest",
                    amount = 500.00,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.INVESTMENTS,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 15),
                    note = "Dollar-cost averaging into Vanguard VOO",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Dinner with Friends at Izakaya",
                    amount = 68.40,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.FOOD_DINING,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    timestamp = now - (oneDay * 3),
                    note = "Ramen & appetizers",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Climbing Gym Monthly Pass",
                    amount = 89.00,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.HEALTH,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    timestamp = now - (oneDay * 12),
                    note = "Bouldering membership",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Cinema IMAX Tickets & Popcorn",
                    amount = 34.00,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.ENTERTAINMENT,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    timestamp = now - (oneDay * 5),
                    note = "Weekend sci-fi premiere",
                    isReconciled = false,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Freelance Consulting Gig",
                    amount = 650.00,
                    type = TransactionType.INCOME,
                    category = TransactionCategory.SALARY,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 8),
                    note = "Mobile UI architectural audit",
                    isReconciled = true,
                    isCloudSynced = true
                )
            )
            dao.insertTransactions(sampleTransactions)

            // 2. Default Budgets
            val defaultBudgets = TransactionCategory.values().map { cat ->
                CategoryBudgetEntity(
                    category = cat.name,
                    monthlyLimit = cat.defaultMonthlyBudget,
                    alertThresholdPercent = 80,
                    isAlertEnabled = true
                )
            }
            dao.insertBudgets(defaultBudgets)

            // 3. Initial Habit Streak (5 days active streak for great onboarding feel)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val initialStreak = StreakEntity(
                id = 1,
                currentStreak = 5,
                longestStreak = 12,
                lastLoggedDate = todayStr,
                streakFreezeCount = 2,
                totalDaysLogged = 24,
                totalXp = 820,
                currentLevel = 4
            )
            dao.insertOrUpdateStreak(initialStreak)

            // 4. Initial Bank Accounts
            val bankAccounts = listOf(
                BankSyncAccountEntity(
                    accountId = "chase_sapphire",
                    institutionName = "Chase Sapphire",
                    accountNumberMask = "•••• 8921",
                    accountType = "Credit Card",
                    balance = -1842.30,
                    lastSyncedTimestamp = now - 1800000L,
                    isAutoSyncActive = true,
                    pendingDiscrepanciesCount = 2
                ),
                BankSyncAccountEntity(
                    accountId = "wells_fargo_checking",
                    institutionName = "Wells Fargo",
                    accountNumberMask = "•••• 3491",
                    accountType = "Checking Account",
                    balance = 5620.40,
                    lastSyncedTimestamp = now - 900000L,
                    isAutoSyncActive = true,
                    pendingDiscrepanciesCount = 0
                ),
                BankSyncAccountEntity(
                    accountId = "apple_card_wallet",
                    institutionName = "Apple Card",
                    accountNumberMask = "•••• 6012",
                    accountType = "Digital Wallet / Titanium",
                    balance = -312.80,
                    lastSyncedTimestamp = now - 3600000L,
                    isAutoSyncActive = true,
                    pendingDiscrepanciesCount = 1
                )
            )
            dao.insertBankAccounts(bankAccounts)

            // 5. Initial User Profile
            val existingProfile = dao.getUserProfileSync()
            if (existingProfile == null) {
                dao.insertOrUpdateUserProfile(
                    UserProfileEntity(
                        id = 1,
                        isOnboardingCompleted = false,
                        userName = "Jordan Walker",
                        userEmail = "jordan.aura@gmail.com",
                        avatarIndex = 0,
                        selectedLanguage = "English",
                        selectedCurrencySymbol = "$",
                        selectedCurrencyCode = "USD",
                        isGoogleCloudSyncActive = true,
                        isAutoExpenseCalculationActive = true,
                        isBankSyncActive = true,
                        primaryLinkedBank = "Chase Sapphire",
                        monthlyBudgetLimit = 2500.0,
                        targetSavingsRatePercent = 30,
                        spendingAlertsEnabled = true,
                        weeklyReportEnabled = true
                    )
                )
            }
        } else {
            // Check if profile exists even if transactions exist
            val existingProfile = dao.getUserProfileSync()
            if (existingProfile == null) {
                dao.insertOrUpdateUserProfile(
                    UserProfileEntity(
                        id = 1,
                        isOnboardingCompleted = true,
                        userName = "Jordan Walker",
                        userEmail = "jordan.aura@gmail.com",
                        avatarIndex = 0,
                        selectedLanguage = "English",
                        selectedCurrencySymbol = "$",
                        selectedCurrencyCode = "USD",
                        isGoogleCloudSyncActive = true,
                        isAutoExpenseCalculationActive = true,
                        isBankSyncActive = true,
                        primaryLinkedBank = "Chase Sapphire",
                        monthlyBudgetLimit = 2500.0,
                        targetSavingsRatePercent = 30,
                        spendingAlertsEnabled = true,
                        weeklyReportEnabled = true
                    )
                )
            }
        }
    }
}
