package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryBudgetEntity
import com.example.data.local.StreakEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.repository.FinancialRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FinancialAppRobolectricTest {

    private lateinit var repository: FinancialRepository
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = AppDatabase.getDatabase(context)
        repository = FinancialRepository(db.finPulseDao())
    }

    @Test
    fun testSeedInitialData_populatesDatabase() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val transactions = repository.allTransactions.first()
        assertTrue("Transactions should not be empty after seeding", transactions.isNotEmpty())

        val streak = repository.userStreak.first()
        assertNotNull("User streak should be initialized", streak)
        assertTrue("Streak should have positive days", (streak?.currentStreak ?: 0) > 0)

        val budgets = repository.allBudgets.first()
        assertTrue("Category budgets should be created", budgets.isNotEmpty())
    }

    @Test
    fun testInsertTransaction_incrementsCountAndEvaluatesBadges() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val initialTxCount = repository.allTransactions.first().size

        val newTx = TransactionEntity(
            title = "Test Matcha Latte",
            amount = 6.50,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD_DINING,
            paymentMethod = PaymentMethod.DIGITAL_WALLET,
            timestamp = System.currentTimeMillis(),
            note = "Robolectric test entry",
            isReconciled = false,
            isCloudSynced = true
        )

        val id = repository.insertTransaction(newTx, ApplicationProvider.getApplicationContext())
        assertTrue("Inserted transaction ID should be positive", id > 0)

        val updatedTransactions = repository.allTransactions.first()
        assertEquals(initialTxCount + 1, updatedTransactions.size)

        val streak = repository.userStreak.first()
        val budgets = repository.allBudgets.first()
        val badges = repository.evaluateBadges(updatedTransactions, streak, budgets)
        assertTrue("At least one badge should be unlocked", badges.any { it.isUnlocked })
    }

    @Test
    fun testReconciliationFlow() = runBlocking {
        repository.seedInitialDataIfEmpty()

        val unreconciled = repository.allTransactions.first().find { !it.isReconciled }
        if (unreconciled != null) {
            repository.reconcileWithBank(unreconciled.id, "bank_ref_test", "Chase Bank")
            val refreshed = repository.allTransactions.first().find { it.id == unreconciled.id }
            assertTrue("Transaction should now be marked as reconciled", refreshed?.isReconciled == true)
        }
    }
}
