package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BankSyncAccountEntity
import com.example.data.local.CategoryBudgetEntity
import com.example.data.local.StreakEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.BadgeItem
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.remote.AiFinancialInsight
import com.example.data.remote.BankFeedTransaction
import com.example.data.remote.CloudSyncStatus
import com.example.data.remote.ParsedExpense
import com.example.data.repository.FinancialRepository
import com.example.ui.components.DaySpendingPoint
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialOverviewState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val netSavingsRate: Float = 0.0f,
    val monthlyBudgetLimit: Double = 0.0,
    val dailyAveragePace: Double = 0.0,
    val categoryBreakdown: Map<TransactionCategory, Double> = emptyMap(),
    val trendPoints: List<DaySpendingPoint> = emptyList(),
    val isCloudSynced: Boolean = true
)

class FinancialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinancialRepository

    val allTransactions: StateFlow<List<TransactionEntity>>
    val allBudgets: StateFlow<List<CategoryBudgetEntity>>
    val userStreak: StateFlow<StreakEntity?>
    val bankAccounts: StateFlow<List<BankSyncAccountEntity>>
    val userProfile: StateFlow<UserProfileEntity?>
    val bankFeed: StateFlow<List<BankFeedTransaction>>
    val cloudSyncStatus: StateFlow<CloudSyncStatus>

    // Overview computation state
    val overviewState: StateFlow<FinancialOverviewState>
    val badges: StateFlow<List<BadgeItem>>

    // AI Financial Coach State
    private val _aiCoachInsight = MutableStateFlow<AiFinancialInsight?>(null)
    val aiCoachInsight: StateFlow<AiFinancialInsight?> = _aiCoachInsight.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Smart Receipt/Expense Parse State
    private val _smartParseResult = MutableStateFlow<ParsedExpense?>(null)
    val smartParseResult: StateFlow<ParsedExpense?> = _smartParseResult.asStateFlow()

    private val _isParsingExpense = MutableStateFlow(false)
    val isParsingExpense: StateFlow<Boolean> = _isParsingExpense.asStateFlow()

    // Celebration Confetti trigger
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    // College Student Perks and Roommate Splits
    private val _studentPerks = MutableStateFlow<List<com.example.data.model.StudentPerk>>(emptyList())
    val studentPerks: StateFlow<List<com.example.data.model.StudentPerk>> = _studentPerks.asStateFlow()

    private val _claimedPerkIds = MutableStateFlow<Set<String>>(emptySet())
    val claimedPerkIds: StateFlow<Set<String>> = _claimedPerkIds.asStateFlow()

    fun togglePerkClaimed(perkId: String) {
        val current = _claimedPerkIds.value
        _claimedPerkIds.value = if (current.contains(perkId)) current - perkId else current + perkId
    }

    private val _roommateSplits = MutableStateFlow<List<com.example.data.model.RoommateSplitRecord>>(
        listOf(
            com.example.data.model.RoommateSplitRecord(
                id = "split_1",
                title = "Maple Dorm 4B High-Speed WiFi & Hulu",
                totalBill = 84.00,
                yourShare = 21.00,
                roommateNames = listOf("Sam", "Alex", "Tyler", "Me"),
                dateLogged = System.currentTimeMillis() - 86400000L * 2,
                isSettled = true
            ),
            com.example.data.model.RoommateSplitRecord(
                id = "split_2",
                title = "Midterm Study Group Late-Night Pizza",
                totalBill = 48.00,
                yourShare = 16.00,
                roommateNames = listOf("Alex", "Sam", "Me"),
                dateLogged = System.currentTimeMillis() - 86400000L * 5,
                isSettled = true
            ),
            com.example.data.model.RoommateSplitRecord(
                id = "split_3",
                title = "Weekend Costco Dorm Supplies & Paper Towels",
                totalBill = 65.50,
                yourShare = 32.75,
                roommateNames = listOf("Tyler", "Me"),
                dateLogged = System.currentTimeMillis() - 86400000L * 1,
                isSettled = false
            )
        )
    )
    val roommateSplits: StateFlow<List<com.example.data.model.RoommateSplitRecord>> = _roommateSplits.asStateFlow()

    init {
        NotificationHelper.initNotificationChannels(application)
        val db = AppDatabase.getDatabase(application)
        repository = FinancialRepository(db.finPulseDao())
        _studentPerks.value = repository.getStudentPerks()

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        allTransactions = repository.allTransactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allBudgets = repository.allBudgets
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userStreak = repository.userStreak
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        bankAccounts = repository.bankAccounts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userProfile = repository.userProfile
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        bankFeed = repository.bankFeed
        cloudSyncStatus = repository.cloudSyncStatus

        // Combine for Financial Overview Computations
        overviewState = combine(allTransactions, allBudgets, bankAccounts) { txList, budgets, banks ->
            computeOverview(txList, budgets, banks)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialOverviewState())

        // Combine for Badges & Gamification
        badges = combine(allTransactions, userStreak, allBudgets) { txList, streak, budgets ->
            repository.evaluateBadges(txList, streak, budgets)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private fun computeOverview(
        transactions: List<TransactionEntity>,
        budgets: List<CategoryBudgetEntity>,
        bankAccounts: List<BankSyncAccountEntity>
    ): FinancialOverviewState {
        val (monthStart, monthEnd) = repository.getCurrentMonthRange()
        val currentMonthTx = transactions.filter { it.timestamp in monthStart..monthEnd }

        val monthlyIncome = currentMonthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val monthlyExpense = currentMonthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val bankTotal = bankAccounts.sumOf { it.balance }
        val allIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val allExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalBalance = if (bankTotal != 0.0) bankTotal else (allIncome - allExpense)

        val netSavingsRate = if (monthlyIncome > 0) {
            ((monthlyIncome - monthlyExpense) / monthlyIncome).toFloat().coerceIn(-1.0f, 1.0f)
        } else 0.0f

        val totalBudget = budgets.sumOf { it.monthlyLimit }

        // Category breakdown
        val breakdown = currentMonthTx
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Trend Points (Last 7 Days)
        val trendPoints = generateLast7DaysTrend(transactions)

        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val dailyAveragePace = if (currentDay > 0) monthlyExpense / currentDay else monthlyExpense

        return FinancialOverviewState(
            totalBalance = totalBalance,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense,
            netSavingsRate = netSavingsRate,
            monthlyBudgetLimit = if (totalBudget > 0) totalBudget else 2500.0,
            dailyAveragePace = dailyAveragePace,
            categoryBreakdown = breakdown,
            trendPoints = trendPoints
        )
    }

    private fun generateLast7DaysTrend(transactions: List<TransactionEntity>): List<DaySpendingPoint> {
        val points = mutableListOf<DaySpendingPoint>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val oneDay = 86400000L

        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() - (i * oneDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = targetCal.timeInMillis
            val endOfDay = startOfDay + oneDay - 1

            val dayTotal = transactions
                .filter { it.type == TransactionType.EXPENSE && it.timestamp in startOfDay..endOfDay }
                .sumOf { it.amount }

            points.add(
                DaySpendingPoint(
                    dayLabel = dayFormat.format(Date(startOfDay)),
                    amount = dayTotal
                )
            )
        }
        return points
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        paymentMethod: PaymentMethod,
        note: String
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                paymentMethod = paymentMethod,
                timestamp = System.currentTimeMillis(),
                note = note,
                isReconciled = false,
                isCloudSynced = true
            )
            repository.insertTransaction(tx, getApplication())
            _showCelebration.value = true
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateBudgetLimit(category: TransactionCategory, newLimit: Double, threshold: Int, alertEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateBudget(
                CategoryBudgetEntity(
                    category = category.name,
                    monthlyLimit = newLimit,
                    alertThresholdPercent = threshold,
                    isAlertEnabled = alertEnabled
                )
            )
        }
    }

    fun reconcileTransaction(localTxId: Long, bankTxId: String, bankRef: String) {
        viewModelScope.launch {
            repository.reconcileWithBank(localTxId, bankTxId, bankRef)
            _showCelebration.value = true
        }
    }

    fun importBankFeedTransaction(bankTx: BankFeedTransaction) {
        viewModelScope.launch {
            repository.importFromBankFeed(bankTx, getApplication())
            _showCelebration.value = true
        }
    }

    fun syncCloud() {
        viewModelScope.launch {
            repository.syncWithCloud()
        }
    }

    fun smartParseExpense(textInput: String) {
        viewModelScope.launch {
            _isParsingExpense.value = true
            val result = repository.geminiService.parseExpenseFromText(textInput)
            _smartParseResult.value = result
            _isParsingExpense.value = false
        }
    }

    fun clearSmartParseResult() {
        _smartParseResult.value = null
    }

    fun requestFinancialCoachAdvice() {
        viewModelScope.launch {
            _isAiThinking.value = true
            val overview = overviewState.value
            val streak = userStreak.value
            val topCategoryEntry = overview.categoryBreakdown.maxByOrNull { it.value }
            val topCatName = topCategoryEntry?.key?.displayName ?: "General"
            val topCatSpent = topCategoryEntry?.value ?: 0.0

            val insight = repository.geminiService.getFinancialCoachAdvice(
                monthlySpent = overview.monthlyExpense,
                monthlyBudget = overview.monthlyBudgetLimit,
                topCategory = topCatName,
                topCategorySpent = topCatSpent,
                savingsRate = overview.netSavingsRate,
                currentStreak = streak?.currentStreak ?: 0
            )
            _aiCoachInsight.value = insight
            _isAiThinking.value = false
        }
    }

    fun triggerTestBudgetAlertNotification(category: TransactionCategory) {
        val budgets = allBudgets.value
        val catBudget = budgets.find { it.category == category.name }?.monthlyLimit ?: category.defaultMonthlyBudget
        NotificationHelper.showBudgetLimitAlert(
            context = getApplication(),
            categoryName = category.displayName,
            spentAmount = catBudget * 0.92,
            limitAmount = catBudget,
            percent = 92
        )
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

    fun completeOnboarding(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.completeOnboarding(profile)
            _showCelebration.value = true
        }
    }

    fun updateUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(isOnboardingCompleted = false))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(selectedLanguage = lang))
        }
    }

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(
                current.copy(
                    selectedCurrencyCode = code,
                    selectedCurrencySymbol = symbol
                )
            )
        }
    }

    fun updatePrimaryBank(bankName: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(primaryLinkedBank = bankName))
        }
    }

    fun toggleCloudSyncSetting(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(isGoogleCloudSyncActive = enabled))
        }
    }

    fun toggleAutoExpenseCalculationSetting(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(isAutoExpenseCalculationActive = enabled))
        }
    }

    fun toggleSpendingAlertsSetting(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(spendingAlertsEnabled = enabled))
        }
    }

    fun addRoommateSplit(
        title: String,
        totalBill: Double,
        roommates: List<String>,
        logMyShareAsExpense: Boolean = true
    ) {
        addRoommateBillSplit(title, totalBill, roommates, logMyShareAsExpense)
    }

    fun findLocalMatchForBankFeed(bankTx: BankFeedTransaction): TransactionEntity? {
        return allTransactions.value.find { local ->
            !local.isReconciled &&
            kotlin.math.abs(local.amount - bankTx.amount) < 0.01 &&
            local.type == TransactionType.EXPENSE
        }
    }

    fun reconcileBankFeedItem(bankTxId: String, localTxId: Long) {
        val bankTx = bankFeed.value.find { it.id == bankTxId }
        val ref = bankTx?.merchant ?: "Direct Bank Match"
        reconcileTransaction(localTxId, bankTxId, ref)
    }

    fun importUnmatchedBankItem(bankTx: BankFeedTransaction) {
        importBankFeedTransaction(bankTx)
    }

    fun addRoommateBillSplit(
        title: String,
        totalBill: Double,
        roommates: List<String>,
        logMyShareAsExpense: Boolean
    ) {
        val myShare = if (roommates.isNotEmpty()) totalBill / roommates.size else totalBill
        val record = com.example.data.model.RoommateSplitRecord(
            id = "split_${System.currentTimeMillis()}",
            title = title,
            totalBill = totalBill,
            yourShare = myShare,
            roommateNames = roommates,
            dateLogged = System.currentTimeMillis(),
            isSettled = false
        )
        _roommateSplits.value = listOf(record) + _roommateSplits.value

        if (logMyShareAsExpense) {
            addTransaction(
                title = "$title (My Share)",
                amount = myShare,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD_DINING,
                paymentMethod = PaymentMethod.VENMO,
                note = "Split with ${roommates.filter { it.lowercase() != "me" }.joinToString(", ")}"
            )
        }
    }

    fun markRoommateSplitSettled(splitId: String) {
        _roommateSplits.value = _roommateSplits.value.map {
            if (it.id == splitId) it.copy(isSettled = !it.isSettled) else it
        }
    }

    fun updateMealPlan(swipesRemaining: Int, flexDollars: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(
                current.copy(
                    diningHallSwipesRemaining = swipesRemaining,
                    flexDiningDollarsRemaining = flexDollars
                )
            )
        }
    }

    fun useMealSwipe() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            val remaining = (current.diningHallSwipesRemaining - 1).coerceAtLeast(0)
            repository.updateUserProfile(current.copy(diningHallSwipesRemaining = remaining))
        }
    }

    fun updateCollegeProfileInfo(
        university: String,
        major: String,
        gradYear: String,
        housing: String,
        mealPlan: String,
        semesterBudget: Double,
        venmoHandle: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(
                current.copy(
                    universityName = university,
                    studentMajor = major,
                    graduationYear = gradYear,
                    campusHousing = housing,
                    diningMealPlan = mealPlan,
                    semesterBudgetLimit = semesterBudget,
                    venmoHandle = venmoHandle
                )
            )
        }
    }
}
