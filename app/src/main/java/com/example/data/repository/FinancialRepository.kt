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
                title = "College Fin Ignition",
                description = "Logged your very first transaction in SpendWise.",
                tier = BadgeTier.BRONZE,
                xpReward = 100,
                progress = if (transactions.isNotEmpty()) 1.0f else 0.0f,
                isUnlocked = transactions.isNotEmpty(),
                unlockedDate = if (transactions.isNotEmpty()) "Unlocked" else null
            ),
            BadgeItem(
                id = "streak_3_days",
                title = "Dorm Discipline",
                description = "Build the habit: Track your daily spending 3 days in a row.",
                tier = BadgeTier.BRONZE,
                xpReward = 150,
                progress = (streakCount / 3.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = streakCount >= 3,
                unlockedDate = if (streakCount >= 3) "Active Streak" else null
            ),
            BadgeItem(
                id = "streak_7_days",
                title = "Midterms Master",
                description = "Maintain a stellar 7-day college expense tracking streak.",
                tier = BadgeTier.SILVER,
                xpReward = 300,
                progress = (streakCount / 7.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = streakCount >= 7,
                unlockedDate = if (streakCount >= 7) "Active Streak" else null
            ),
            BadgeItem(
                id = "streak_30_days",
                title = "Semester Money Sensei",
                description = "Master financial mindfulness with a 30-day streak all semester long.",
                tier = BadgeTier.DIAMOND,
                xpReward = 1000,
                progress = (streakCount / 30.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = streakCount >= 30
            ),
            BadgeItem(
                id = "roommate_ace",
                title = "Roommate Split Ace",
                description = "Split and reconcile shared dorm bills & pizza runs with zero friction.",
                tier = BadgeTier.SILVER,
                xpReward = 250,
                progress = (reconciledCount / 3.0f).coerceIn(0.0f, 1.0f),
                isUnlocked = reconciledCount >= 3,
                unlockedDate = if (reconciledCount >= 3) "Mastered" else null
            ),
            BadgeItem(
                id = "budget_guardian",
                title = "Campus Budget Guardian",
                description = "Stay under your monthly college allowance & burn rate limit.",
                tier = BadgeTier.GOLD,
                xpReward = 500,
                progress = if (totalBudget > 0 && totalExpenses <= totalBudget) 1.0f else 0.7f,
                isUnlocked = totalBudget > 0 && totalExpenses <= totalBudget,
                unlockedDate = if (totalBudget > 0 && totalExpenses <= totalBudget) "Current Month" else null
            ),
            BadgeItem(
                id = "cloud_architect",
                title = "Campus Cloud Sync",
                description = "Encrypted multi-device backup active with real-time sync.",
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

            // 1. Initial College Sample Transactions
            val sampleTransactions = listOf(
                TransactionEntity(
                    title = "Fall Financial Aid & Scholarship Refund",
                    amount = 2200.00,
                    type = TransactionType.INCOME,
                    category = TransactionCategory.STUDENT_AID_ALLOWANCE,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 20),
                    note = "University Direct Deposit Financial Aid Office",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Campus Work-Study Library Paycheck",
                    amount = 340.00,
                    type = TransactionType.INCOME,
                    category = TransactionCategory.CAMPUS_JOB_SALARY,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 6),
                    note = "Bi-weekly 20 hrs student desk assistant",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Campus Bookstore - Algorithms Textbook & Notebooks",
                    amount = 68.50,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.TEXTBOOKS_TUITION,
                    paymentMethod = PaymentMethod.DEBIT_CARD,
                    timestamp = now - (oneDay * 12),
                    note = "Rented used textbook + lab manual",
                    isReconciled = true,
                    bankTransactionRef = "bank_tx_101",
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Dorm Late-Night Pizza Run (Split w/ Roommates)",
                    amount = 18.75,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.FOOD_DINING,
                    paymentMethod = PaymentMethod.VENMO,
                    timestamp = now - (oneDay * 1),
                    note = "My share of large pizza & drinks with Sam & Alex",
                    isReconciled = true,
                    bankTransactionRef = "bank_tx_102",
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Maple Hall Dorm Rent & Utilities",
                    amount = 550.00,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.HOUSING_DORM,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    timestamp = now - (oneDay * 22),
                    note = "Monthly student housing fee",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Campus Coffee Cart - Double Cold Brew & Bagel",
                    amount = 6.25,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.COFFEE_ENERGY,
                    paymentMethod = PaymentMethod.CAMPUS_CARD,
                    timestamp = now - (3600000L * 3), // 3 hours ago
                    note = "Morning fuel for 9 AM Physics lecture",
                    isReconciled = false,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Trader Joe's Dorm Snacks & Oat Milk",
                    amount = 42.10,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.GROCERIES,
                    paymentMethod = PaymentMethod.APPLE_PAY,
                    timestamp = now - (oneDay * 3),
                    note = "Microwave ramen, apples, Greek yogurt, peanut butter",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Spotify & Hulu Student Bundle",
                    amount = 5.99,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.SUBSCRIPTIONS_TECH,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    timestamp = now - (oneDay * 10),
                    note = "Monthly verified .edu subscription",
                    isReconciled = true,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Late Night Uber Share to Campus",
                    amount = 12.40,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.TRANSPORT,
                    paymentMethod = PaymentMethod.VENMO,
                    timestamp = now - (oneDay * 4),
                    note = "Split rideshare after study group at library",
                    isReconciled = false,
                    isCloudSynced = true
                ),
                TransactionEntity(
                    title = "Campus Print Station - Engineering Lab Reports",
                    amount = 8.50,
                    type = TransactionType.EXPENSE,
                    category = TransactionCategory.CAMPUS_SUPPLIES,
                    paymentMethod = PaymentMethod.CAMPUS_CARD,
                    timestamp = now - (oneDay * 7),
                    note = "Color poster print & binding",
                    isReconciled = true,
                    isCloudSynced = true
                )
            )
            dao.insertTransactions(sampleTransactions)

            // 2. Default College Budgets
            val defaultBudgets = TransactionCategory.values().map { cat ->
                CategoryBudgetEntity(
                    category = cat.name,
                    monthlyLimit = cat.defaultMonthlyBudget,
                    alertThresholdPercent = 80,
                    isAlertEnabled = true
                )
            }
            dao.insertBudgets(defaultBudgets)

            // 3. Initial Habit Streak (5 days active streak)
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
                    accountId = "campus_checking",
                    institutionName = "Campus Credit Union Checking",
                    accountNumberMask = "•••• 4219",
                    accountType = "Checking / Financial Aid",
                    balance = 1845.50,
                    lastSyncedTimestamp = now - 900000L,
                    isAutoSyncActive = true,
                    pendingDiscrepanciesCount = 1
                ),
                BankSyncAccountEntity(
                    accountId = "student_credit_card",
                    institutionName = "Chase Freedom Student",
                    accountNumberMask = "•••• 8921",
                    accountType = "Student Credit Card (1.5% Cash Back)",
                    balance = -214.30,
                    lastSyncedTimestamp = now - 1800000L,
                    isAutoSyncActive = true,
                    pendingDiscrepanciesCount = 1
                ),
                BankSyncAccountEntity(
                    accountId = "campus_card_points",
                    institutionName = "University Dining Dollars & Flex",
                    accountNumberMask = "ID: 2027-9941",
                    accountType = "Meal Plan & Swipes",
                    balance = 142.50,
                    lastSyncedTimestamp = now - 3600000L,
                    isAutoSyncActive = true,
                    pendingDiscrepanciesCount = 0
                )
            )
            dao.insertBankAccounts(bankAccounts)

            // 5. Initial User Profile for College Student
            val existingProfile = dao.getUserProfileSync()
            if (existingProfile == null) {
                dao.insertOrUpdateUserProfile(
                    UserProfileEntity(
                        id = 1,
                        isOnboardingCompleted = false,
                        userName = "Jordan Walker",
                        userEmail = "jordan.spendwise@gmail.com",
                        avatarIndex = 0,
                        selectedLanguage = "English",
                        selectedCurrencySymbol = "$",
                        selectedCurrencyCode = "USD",
                        isGoogleCloudSyncActive = true,
                        isAutoExpenseCalculationActive = true,
                        isBankSyncActive = true,
                        primaryLinkedBank = "Campus Credit Union",
                        monthlyBudgetLimit = 1200.0,
                        targetSavingsRatePercent = 20,
                        spendingAlertsEnabled = true,
                        weeklyReportEnabled = true,
                        universityName = "State University",
                        studentMajor = "Computer Science",
                        graduationYear = "Class of '27",
                        semesterTerm = "Fall 2026",
                        campusHousing = "Campus Dorm (Maple Hall)",
                        diningMealPlan = "14 Meals/Wk + $200 Points",
                        semesterBudgetLimit = 4500.0,
                        semesterWeeksTotal = 16,
                        semesterWeeksElapsed = 5,
                        diningHallSwipesRemaining = 94,
                        flexDiningDollarsRemaining = 142.50,
                        venmoHandle = "@jordan-spendwise",
                        isCollegeModeActive = true
                    )
                )
            }
        }
    }

    fun getStudentPerks(): List<com.example.data.model.StudentPerk> {
        return listOf(
            com.example.data.model.StudentPerk(
                id = "spotify_student",
                company = "Spotify + Hulu",
                title = "Spotify Premium Student with Hulu",
                discountSummary = "$5.99/mo (Normally $11.99)",
                category = "Music & Video",
                verifiedMethod = "SheerID / .edu Email",
                savingsEstimate = "Save $132/year",
                urlHint = "spotify.com/student"
            ),
            com.example.data.model.StudentPerk(
                id = "prime_student",
                company = "Amazon Prime",
                title = "Prime Student 6-Month Free Trial",
                discountSummary = "Free for 6 Months, then 50% Off",
                category = "Shopping & Fast Shipping",
                verifiedMethod = ".edu Email",
                savingsEstimate = "Save $75.00",
                urlHint = "amazon.com/joinstudent"
            ),
            com.example.data.model.StudentPerk(
                id = "github_student",
                company = "GitHub Education",
                title = "GitHub Student Developer Pack",
                discountSummary = "100% Free Tools, Copilot & Domains",
                category = "Tech & Software",
                verifiedMethod = "Student ID / .edu Email",
                savingsEstimate = "Worth $2,000+",
                urlHint = "education.github.com/pack"
            ),
            com.example.data.model.StudentPerk(
                id = "apple_education",
                company = "Apple",
                title = "Apple Education Pricing + Gift Card",
                discountSummary = "Up to $150 Gift Card + 10% Off Macs",
                category = "Tech & Software",
                verifiedMethod = "UNiDAYS Verification",
                savingsEstimate = "Save $150+",
                urlHint = "apple.com/us-edu/shop"
            ),
            com.example.data.model.StudentPerk(
                id = "notion_student",
                company = "Notion",
                title = "Notion Plus Plan for Students",
                discountSummary = "100% Free Personal Pro Workspace",
                category = "Productivity & Notes",
                verifiedMethod = ".edu Email",
                savingsEstimate = "Save $96/year",
                urlHint = "notion.so/students"
            ),
            com.example.data.model.StudentPerk(
                id = "chegg_rentals",
                company = "Chegg / CampusBooks",
                title = "Used Textbooks & Textbook Rentals",
                discountSummary = "Up to 85% Off List Prices",
                category = "Course Materials",
                verifiedMethod = "Instant Online",
                savingsEstimate = "Save ~$250/semester",
                urlHint = "chegg.com/books"
            ),
            com.example.data.model.StudentPerk(
                id = "unidays_fashion",
                company = "UNiDAYS & Nike/ASOS",
                title = "15-20% Off Apparel & Footwear",
                discountSummary = "15% off Nike, ASOS, Levi's & H&M",
                category = "Retail & Lifestyle",
                verifiedMethod = "UNiDAYS App",
                savingsEstimate = "Save 15% on gear",
                urlHint = "myunidays.com"
            )
        )
    }
}

