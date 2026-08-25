package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedExpense(
    val title: String,
    val amount: Double,
    val category: TransactionCategory,
    val paymentMethod: PaymentMethod,
    val note: String
)

data class AiFinancialInsight(
    val summary: String,
    val actionItems: List<String>,
    val encouragement: String,
    val projectedSavings: Double
)

class GeminiFinancialService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun parseExpenseFromText(input: String): ParsedExpense = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackParse(input)
        }

        try {
            val prompt = """
                Extract expense details from this input: "$input".
                Respond ONLY with a valid JSON object matching this schema:
                {
                  "title": "Short title (e.g. Starbucks, Grocery Run, Uber)",
                  "amount": 12.50,
                  "category": "FOOD_DINING" or "GROCERIES" or "TRANSPORT" or "HOUSING_BILLS" or "SHOPPING" or "ENTERTAINMENT" or "HEALTH" or "INVESTMENTS" or "UTILITIES" or "TRAVEL" or "OTHER",
                  "paymentMethod": "CREDIT_CARD" or "DEBIT_CARD" or "BANK_TRANSFER" or "CASH" or "DIGITAL_WALLET",
                  "note": "Optional short note"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBody.isEmpty()) {
                return@withContext fallbackParse(input)
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            val parsedJson = JSONObject(text)
            val title = parsedJson.optString("title", "Expense")
            val amount = parsedJson.optDouble("amount", 0.0)
            val categoryStr = parsedJson.optString("category", "OTHER")
            val paymentMethodStr = parsedJson.optString("paymentMethod", "CREDIT_CARD")
            val note = parsedJson.optString("note", "")

            ParsedExpense(
                title = title.ifEmpty { "Expense" },
                amount = if (amount > 0) amount else 10.0,
                category = TransactionCategory.fromString(categoryStr),
                paymentMethod = try { PaymentMethod.valueOf(paymentMethodStr) } catch (e: Exception) { PaymentMethod.CREDIT_CARD },
                note = note
            )
        } catch (e: Exception) {
            fallbackParse(input)
        }
    }

    suspend fun getFinancialCoachAdvice(
        monthlySpent: Double,
        monthlyBudget: Double,
        topCategory: String,
        topCategorySpent: Double,
        savingsRate: Float,
        currentStreak: Int
    ): AiFinancialInsight = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackCoachAdvice(monthlySpent, monthlyBudget, topCategory, topCategorySpent, savingsRate, currentStreak)
        }

        try {
            val prompt = """
                You are FinPulse AI, an empathetic, highly encouraging financial habit coach.
                Analyze the user's spending data:
                - Total Spent This Month: $$monthlySpent (Monthly Budget Limit: $$monthlyBudget)
                - Highest Spending Category: $topCategory ($$topCategorySpent)
                - Current Savings Rate: ${(savingsRate * 100).toInt()}%
                - Daily Budgeting Streak: $currentStreak days

                Respond ONLY in JSON format:
                {
                  "summary": "1-2 punchy sentences assessing current spending pace and budget health.",
                  "actionItems": [
                    "Actionable micro-habit 1 to save money or optimize this week",
                    "Actionable micro-habit 2",
                    "Actionable micro-habit 3"
                  ],
                  "encouragement": "An inspiring, positive affirmation reinforcing healthy money habits and rewarding their streak.",
                  "projectedSavings": 120.0
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBody.isEmpty()) {
                return@withContext fallbackCoachAdvice(monthlySpent, monthlyBudget, topCategory, topCategorySpent, savingsRate, currentStreak)
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            val parsedJson = JSONObject(text)
            val summary = parsedJson.optString("summary", "You are tracking your finances with great awareness.")
            val actionItemsArray = parsedJson.optJSONArray("actionItems")
            val actionList = mutableListOf<String>()
            if (actionItemsArray != null) {
                for (i in 0 until actionItemsArray.length()) {
                    actionList.add(actionItemsArray.getString(i))
                }
            }
            val encouragement = parsedJson.optString("encouragement", "Consistency builds long-term wealth. Keep your streak alive!")
            val projectedSavings = parsedJson.optDouble("projectedSavings", 85.0)

            AiFinancialInsight(
                summary = summary,
                actionItems = if (actionList.isNotEmpty()) actionList else listOf(
                    "Review dining out expenses this weekend",
                    "Automate a 5% transfer to your emergency fund",
                    "Keep your daily logging streak going"
                ),
                encouragement = encouragement,
                projectedSavings = projectedSavings
            )
        } catch (e: Exception) {
            fallbackCoachAdvice(monthlySpent, monthlyBudget, topCategory, topCategorySpent, savingsRate, currentStreak)
        }
    }

    private fun fallbackParse(input: String): ParsedExpense {
        // Smart regex extractor for offline/fallback
        val amountRegex = """\$?(\d+(?:\.\d{1,2})?)""".toRegex()
        val match = amountRegex.find(input)
        val amount = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 24.50

        val lower = input.lowercase()
        val category = when {
            lower.contains("coffee") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("burger") || lower.contains("pizza") || lower.contains("restaurant") || lower.contains("food") -> TransactionCategory.FOOD_DINING
            lower.contains("grocer") || lower.contains("market") || lower.contains("walmart") || lower.contains("trader") || lower.contains("costco") -> TransactionCategory.GROCERIES
            lower.contains("uber") || lower.contains("lyft") || lower.contains("gas") || lower.contains("fuel") || lower.contains("subway") || lower.contains("metro") || lower.contains("flight") -> TransactionCategory.TRANSPORT
            lower.contains("rent") || lower.contains("electric") || lower.contains("water") || lower.contains("wifi") || lower.contains("utility") -> TransactionCategory.HOUSING_BILLS
            lower.contains("movie") || lower.contains("cinema") || lower.contains("netflix") || lower.contains("spotify") || lower.contains("game") -> TransactionCategory.ENTERTAINMENT
            lower.contains("gym") || lower.contains("doctor") || lower.contains("pharmacy") || lower.contains("medicine") -> TransactionCategory.HEALTH
            lower.contains("cloth") || lower.contains("amazon") || lower.contains("shoes") || lower.contains("shopping") -> TransactionCategory.SHOPPING
            lower.contains("stock") || lower.contains("crypto") || lower.contains("deposit") || lower.contains("etf") -> TransactionCategory.INVESTMENTS
            else -> TransactionCategory.OTHER
        }

        val title = input.replace(amountRegex, "").trim().take(30).ifEmpty { "Quick Expense" }

        return ParsedExpense(
            title = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            amount = amount,
            category = category,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            note = "Logged via Smart Parser"
        )
    }

    private fun fallbackCoachAdvice(
        monthlySpent: Double,
        monthlyBudget: Double,
        topCategory: String,
        topCategorySpent: Double,
        savingsRate: Float,
        currentStreak: Int
    ): AiFinancialInsight {
        val pct = if (monthlyBudget > 0) (monthlySpent / monthlyBudget * 100).toInt() else 65
        val summary = if (pct <= 80) {
            "Excellent discipline! You've used $pct% of your monthly budget and your daily logging streak is active at $currentStreak days."
        } else {
            "You are at $pct% of your budget limit. Pacing your discretionary spend in $topCategory will keep you safe for the rest of the month."
        }

        return AiFinancialInsight(
            summary = summary,
            actionItems = listOf(
                "Cap $topCategory expenses by setting a daily \$35 micro-limit",
                "Lock in your $currentStreak-day streak bonus by logging today's lunch",
                "Move an extra \$50 into high-yield savings to boost your savings rate"
            ),
            encouragement = "Mindful spending is a muscle that strengthens with every entry. You are in total control of your financial destiny!",
            projectedSavings = 145.0
        )
    }
}
