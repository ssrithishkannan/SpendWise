package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val displayName: String) {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Account"),
    CASH("Cash"),
    DIGITAL_WALLET("Digital Wallet")
}

enum class TransactionCategory(
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val defaultMonthlyBudget: Double
) {
    FOOD_DINING("Food & Dining", Color(0xFFFF6F59), Icons.Default.Fastfood, 500.0),
    GROCERIES("Groceries", Color(0xFF10B981), Icons.Default.LocalGroceryStore, 400.0),
    TRANSPORT("Transport & Travel", Color(0xFF3B82F6), Icons.Default.DirectionsCar, 250.0),
    HOUSING_BILLS("Housing & Utilities", Color(0xFF8B5CF6), Icons.Default.Home, 1200.0),
    SHOPPING("Shopping", Color(0xFFEC4899), Icons.Default.ShoppingBag, 300.0),
    ENTERTAINMENT("Entertainment", Color(0xFFF59E0B), Icons.Default.Movie, 200.0),
    HEALTH("Health & Fitness", Color(0xFF14B8A6), Icons.Default.MedicalServices, 150.0),
    INVESTMENTS("Investments & Savings", Color(0xFF059669), Icons.Default.TrendingUp, 600.0),
    UTILITIES("Utilities & Bills", Color(0xFFF97316), Icons.Default.Lightbulb, 180.0),
    TRAVEL("Travel & Vacation", Color(0xFF0284C7), Icons.Default.Flight, 200.0),
    SALARY("Salary & Income", Color(0xFF22C55E), Icons.Default.Work, 0.0),
    OTHER("Other", Color(0xFF6B7280), Icons.Default.Category, 150.0);

    companion object {
        fun fromString(value: String): TransactionCategory {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                OTHER
            }
        }
    }
}

enum class BadgeTier(val color: Color, val title: String) {
    BRONZE(Color(0xFFCD7F32), "Bronze"),
    SILVER(Color(0xFFC0C0C0), "Silver"),
    GOLD(Color(0xFFFFD700), "Gold"),
    DIAMOND(Color(0xFF00E5FF), "Diamond")
}

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val tier: BadgeTier,
    val xpReward: Int,
    val progress: Float, // 0.0f to 1.0f
    val isUnlocked: Boolean,
    val unlockedDate: String? = null
)
