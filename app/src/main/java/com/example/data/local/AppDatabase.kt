package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        try { TransactionType.valueOf(value) } catch (e: Exception) { TransactionType.EXPENSE }

    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory): String = category.name

    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory =
        TransactionCategory.fromString(value)

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod =
        try { PaymentMethod.valueOf(value) } catch (e: Exception) { PaymentMethod.CREDIT_CARD }
}

@Database(
    entities = [
        TransactionEntity::class,
        CategoryBudgetEntity::class,
        StreakEntity::class,
        BankSyncAccountEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun finPulseDao(): FinPulseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finpulse_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
