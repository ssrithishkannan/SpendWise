package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinPulseDao {

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isReconciled = 0 ORDER BY timestamp DESC")
    fun getUnreconciledTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("UPDATE transactions SET isReconciled = :reconciled, bankTransactionRef = :bankRef WHERE id = :id")
    suspend fun setReconciliationStatus(id: Long, reconciled: Boolean, bankRef: String?)

    // --- Budgets ---
    @Query("SELECT * FROM category_budgets")
    fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CategoryBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<CategoryBudgetEntity>)

    @Update
    suspend fun updateBudget(budget: CategoryBudgetEntity)

    // --- Streak & Level ---
    @Query("SELECT * FROM user_streak WHERE id = 1 LIMIT 1")
    fun getStreak(): Flow<StreakEntity?>

    @Query("SELECT * FROM user_streak WHERE id = 1 LIMIT 1")
    suspend fun getStreakSync(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: StreakEntity)

    // --- Bank Accounts ---
    @Query("SELECT * FROM bank_accounts")
    fun getAllBankAccounts(): Flow<List<BankSyncAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccounts(accounts: List<BankSyncAccountEntity>)

    @Update
    suspend fun updateBankAccount(account: BankSyncAccountEntity)

    // --- User Profile & Setup Preferences ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserProfile(profile: UserProfileEntity)
}
