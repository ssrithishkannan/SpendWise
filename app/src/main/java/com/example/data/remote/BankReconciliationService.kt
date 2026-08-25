package com.example.data.remote

import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BankFeedTransaction(
    val id: String = UUID.randomUUID().toString(),
    val institution: String,
    val merchant: String,
    val amount: Double,
    val date: String,
    val timestamp: Long,
    val suggestedCategory: TransactionCategory,
    val isMatchedWithLocal: Boolean = false,
    val localTransactionId: Long? = null,
    val status: BankMatchStatus = BankMatchStatus.PENDING
)

enum class BankMatchStatus {
    MATCHED,
    PENDING,
    DISCREPANCY,
    IMPORTED
}

data class CloudSyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "Just now",
    val pendingChangesCount: Int = 0,
    val cloudAccountsConnected: Int = 3,
    val syncHealthPercent: Int = 100
)

class BankReconciliationService {

    private val _bankFeed = MutableStateFlow<List<BankFeedTransaction>>(emptyList())
    val bankFeed: StateFlow<List<BankFeedTransaction>> = _bankFeed.asStateFlow()

    private val _cloudSyncState = MutableStateFlow(CloudSyncStatus())
    val cloudSyncState: StateFlow<CloudSyncStatus> = _cloudSyncState.asStateFlow()

    init {
        initializeSampleFeed()
    }

    private fun initializeSampleFeed() {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

        val initialItems = listOf(
            BankFeedTransaction(
                id = "bank_tx_101",
                institution = "Chase Sapphire Preferred",
                merchant = "WHOLE FOODS MARKET #108",
                amount = 64.20,
                date = dateFormat.format(Date(now - oneDay)),
                timestamp = now - oneDay,
                suggestedCategory = TransactionCategory.GROCERIES,
                isMatchedWithLocal = true,
                status = BankMatchStatus.MATCHED
            ),
            BankFeedTransaction(
                id = "bank_tx_102",
                institution = "Wells Fargo Checking",
                merchant = "UBER TRIP HELP.UBER.COM",
                amount = 23.50,
                date = dateFormat.format(Date(now - (oneDay * 2))),
                timestamp = now - (oneDay * 2),
                suggestedCategory = TransactionCategory.TRANSPORT,
                isMatchedWithLocal = true,
                status = BankMatchStatus.MATCHED
            ),
            BankFeedTransaction(
                id = "bank_tx_103",
                institution = "Apple Card (Mastercard)",
                merchant = "STARBUCKS STORE #8942",
                amount = 6.75,
                date = dateFormat.format(Date(now - (oneDay / 2))),
                timestamp = now - (oneDay / 2),
                suggestedCategory = TransactionCategory.FOOD_DINING,
                isMatchedWithLocal = false,
                status = BankMatchStatus.PENDING
            ),
            BankFeedTransaction(
                id = "bank_tx_104",
                institution = "Chase Sapphire Preferred",
                merchant = "AMAZON.COM*DIGITAL_ORD",
                amount = 45.99,
                date = dateFormat.format(Date(now - (oneDay * 3))),
                timestamp = now - (oneDay * 3),
                suggestedCategory = TransactionCategory.CAMPUS_SUPPLIES,
                isMatchedWithLocal = false,
                status = BankMatchStatus.PENDING
            ),
            BankFeedTransaction(
                id = "bank_tx_105",
                institution = "Wells Fargo Checking",
                merchant = "PACIFIC GAS & ELECTRIC",
                amount = 112.40,
                date = dateFormat.format(Date(now - (oneDay * 4))),
                timestamp = now - (oneDay * 4),
                suggestedCategory = TransactionCategory.HOUSING_DORM,
                isMatchedWithLocal = false,
                status = BankMatchStatus.PENDING
            )
        )
        _bankFeed.value = initialItems
    }

    fun markTransactionMatched(bankTxId: String, localId: Long) {
        _bankFeed.value = _bankFeed.value.map { item ->
            if (item.id == bankTxId) {
                item.copy(isMatchedWithLocal = true, localTransactionId = localId, status = BankMatchStatus.MATCHED)
            } else {
                item
            }
        }
    }

    fun markTransactionImported(bankTxId: String, localId: Long) {
        _bankFeed.value = _bankFeed.value.map { item ->
            if (item.id == bankTxId) {
                item.copy(isMatchedWithLocal = true, localTransactionId = localId, status = BankMatchStatus.IMPORTED)
            } else {
                item
            }
        }
    }

    suspend fun performCloudSync() {
        _cloudSyncState.value = _cloudSyncState.value.copy(isSyncing = true)
        kotlinx.coroutines.delay(1200) // Simulate fast reliable sync
        val nowFormatted = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        _cloudSyncState.value = CloudSyncStatus(
            isSyncing = false,
            lastSyncTime = "$nowFormatted (Encrypted Cloud)",
            pendingChangesCount = 0,
            cloudAccountsConnected = 3,
            syncHealthPercent = 100
        )
    }
}
