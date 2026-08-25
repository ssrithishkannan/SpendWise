package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val displayName: String) {
    VENMO("Venmo / Zelle"),
    CAMPUS_CARD("Campus Card / Dining Points"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Student Credit Card"),
    APPLE_PAY("Apple Pay / Google Pay"),
    CASH("Cash"),
    BANK_TRANSFER("Direct Deposit / Financial Aid")
}

enum class TransactionCategory(
    val displayName: String,
    val color: Color,
    val icon: ImageVector,
    val defaultMonthlyBudget: Double,
    val collegeTip: String = ""
) {
    FOOD_DINING("Dining Hall & Campus Eats", Color(0xFFFF6F59), Icons.Default.Fastfood, 250.0, "Use dining hall swipes before ordering takeout!"),
    GROCERIES("Dorm Groceries & Snacks", Color(0xFF10B981), Icons.Default.LocalGroceryStore, 180.0, "Buy bulk staples (oats, pasta, peanut butter) to save 40%."),
    TEXTBOOKS_TUITION("Textbooks & Course Packs", Color(0xFF6366F1), Icons.Default.MenuBook, 150.0, "Rent on Chegg, buy used, or check campus library reserve first!"),
    HOUSING_DORM("Dorm, Rent & Utilities", Color(0xFF8B5CF6), Icons.Default.Home, 650.0, "Split wifi & utility bills with roommates instantly."),
    COFFEE_ENERGY("Coffee & Late-Night Fuel", Color(0xFFD97706), Icons.Default.Coffee, 45.0, "Making drip coffee in your dorm saves ~$80 every month."),
    TRANSPORT("Campus Transit & Rideshare", Color(0xFF3B82F6), Icons.Default.DirectionsCar, 75.0, "Take campus shuttles or carpool for weekend trips."),
    NIGHT_OUT_SOCIAL("Weekends & Student Clubs", Color(0xFFF43F5E), Icons.Default.Celebration, 100.0, "Look for student discount nights & free campus events."),
    SUBSCRIPTIONS_TECH("Student Apps & Software", Color(0xFF06B6D4), Icons.Default.Laptop, 35.0, "Use .edu discounts for Spotify, Apple Music & Prime Student!"),
    CAMPUS_SUPPLIES("Printing & Lab Supplies", Color(0xFFEC4899), Icons.Default.School, 40.0, "Check if your department gives free printing credits."),
    STUDENT_AID_ALLOWANCE("Financial Aid & Allowance", Color(0xFF10B981), Icons.Default.AccountBalance, 0.0, "Disbursements & monthly parent/guardian support."),
    CAMPUS_JOB_SALARY("Work-Study & Wages", Color(0xFF22C55E), Icons.Default.Work, 0.0, "On-campus job hours, tutoring, or internship stipends."),
    ENTERTAINMENT("Gaming, Movies & Concerts", Color(0xFFF59E0B), Icons.Default.Movie, 60.0, "Campus recreation center has free equipment rentals!"),
    HEALTH("Health, Pharmacy & Gym", Color(0xFF14B8A6), Icons.Default.MedicalServices, 40.0, "Campus health center covers basic checkups & meds."),
    INVESTMENTS("Emergency Fund & Savings", Color(0xFF059669), Icons.Default.TrendingUp, 50.0, "Building a $500 emergency buffer protects you from surprise costs."),
    OTHER("Miscellaneous College", Color(0xFF6B7280), Icons.Default.Category, 40.0, "Laundry coins, campus parking, and unexpected fees.");

    companion object {
        fun fromString(value: String): TransactionCategory {
            return try {
                valueOf(value)
            } catch (e: Exception) {
                // Compatibility fallback for previous category keys
                when (value) {
                    "HOUSING_BILLS" -> HOUSING_DORM
                    "SHOPPING" -> CAMPUS_SUPPLIES
                    "SALARY" -> CAMPUS_JOB_SALARY
                    "TRAVEL" -> TRANSPORT
                    "UTILITIES" -> HOUSING_DORM
                    else -> OTHER
                }
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

data class StudentPerk(
    val id: String,
    val company: String,
    val title: String,
    val discountSummary: String,
    val category: String, // "Tech & Software", "Music & Video", "Food & Dining", "Retail", "Travel"
    val verifiedMethod: String, // ".edu Email", "UNiDAYS", "Student Beans", "SheerID"
    val savingsEstimate: String,
    val urlHint: String = ""
)

data class RoommateSplitRecord(
    val id: String,
    val title: String,
    val totalBill: Double,
    val yourShare: Double,
    val roommateNames: List<String>,
    val dateLogged: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false
)

