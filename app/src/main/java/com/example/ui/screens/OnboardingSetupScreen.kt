package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.PolishGreenSuccess
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishPrimaryDark
import com.example.ui.theme.PolishPrimaryLight
import com.example.ui.theme.PolishSecondary
import com.example.ui.theme.PolishTertiaryGreen

data class LanguageOption(val code: String, val name: String, val nativeName: String, val flag: String)
data class CurrencyOption(val code: String, val symbol: String, val name: String)
data class BankOption(val name: String, val type: String, val icon: ImageVector, val popular: Boolean)

val AVAILABLE_LANGUAGES = listOf(
    LanguageOption("en", "English", "English", "🇺🇸"),
    LanguageOption("es", "Spanish", "Español", "🇪🇸"),
    LanguageOption("fr", "French", "Français", "🇫🇷"),
    LanguageOption("de", "German", "Deutsch", "🇩🇪"),
    LanguageOption("ja", "Japanese", "日本語", "🇯🇵"),
    LanguageOption("hi", "Hindi", "हिन्दी", "🇮🇳")
)

val AVAILABLE_CURRENCIES = listOf(
    CurrencyOption("USD", "$", "US Dollar ($)"),
    CurrencyOption("EUR", "€", "Euro (€)"),
    CurrencyOption("GBP", "£", "British Pound (£)"),
    CurrencyOption("JPY", "¥", "Japanese Yen (¥)"),
    CurrencyOption("INR", "₹", "Indian Rupee (₹)"),
    CurrencyOption("CAD", "CA$", "Canadian Dollar (CA$)"),
    CurrencyOption("AUD", "AU$", "Australian Dollar (AU$)")
)

val AVAILABLE_BANKS = listOf(
    BankOption("Chase Bank", "Checking & Sapphire Card", Icons.Default.AccountBalance, true),
    BankOption("Bank of America", "Advantage Banking", Icons.Default.AccountBalance, true),
    BankOption("Wells Fargo", "Way2Save & Active Cash", Icons.Default.AccountBalance, false),
    BankOption("Apple Card", "Titanium & Apple Pay", Icons.Default.AccountBalance, true),
    BankOption("Citibank", "Double Cash Account", Icons.Default.AccountBalance, false),
    BankOption("Manual Cash Ledger", "Offline On-Device Only", Icons.Default.ReceiptLong, false)
)

val AVATAR_COLORS = listOf(
    Color(0xFF0061A4),
    Color(0xFF006C50),
    Color(0xFF6750A4),
    Color(0xFF984061),
    Color(0xFFB42318),
    Color(0xFF0F766E)
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingSetupScreen(
    currentProfile: UserProfileEntity?,
    onComplete: (UserProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // State collected across steps
    var userName by remember { mutableStateOf(currentProfile?.userName ?: "Jordan Walker") }
    var userEmail by remember { mutableStateOf(currentProfile?.userEmail ?: "jordan.aura@gmail.com") }
    var selectedAvatarIndex by remember { mutableIntStateOf(currentProfile?.avatarIndex ?: 0) }
    var selectedLanguage by remember { mutableStateOf(currentProfile?.selectedLanguage ?: "English") }
    var selectedCurrencyCode by remember { mutableStateOf(currentProfile?.selectedCurrencyCode ?: "USD") }
    var selectedCurrencySymbol by remember { mutableStateOf(currentProfile?.selectedCurrencySymbol ?: "$") }
    var isGoogleSyncActive by remember { mutableStateOf(currentProfile?.isGoogleCloudSyncActive ?: true) }
    var isBankSyncActive by remember { mutableStateOf(currentProfile?.isBankSyncActive ?: true) }
    var selectedBank by remember { mutableStateOf(currentProfile?.primaryLinkedBank ?: "Chase Bank") }
    var isAutoCalculateActive by remember { mutableStateOf(currentProfile?.isAutoExpenseCalculationActive ?: true) }
    var isSpendingAlertsActive by remember { mutableStateOf(currentProfile?.spendingAlertsEnabled ?: true) }
    var monthlyBudget by remember { mutableFloatStateOf((currentProfile?.monthlyBudgetLimit ?: 2500.0).toFloat()) }
    var savingsTargetRate by remember { mutableIntStateOf(currentProfile?.targetSavingsRatePercent ?: 30) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_setup_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header with Progress & Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous Step",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SPENDWISE SETUP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Step $currentStep of $totalSteps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            val finalProfile = UserProfileEntity(
                                id = 1,
                                isOnboardingCompleted = true,
                                userName = userName.ifBlank { "Jordan Walker" },
                                userEmail = userEmail.ifBlank { "jordan.spendwise@gmail.com" },
                                avatarIndex = selectedAvatarIndex,
                                selectedLanguage = selectedLanguage,
                                selectedCurrencySymbol = selectedCurrencySymbol,
                                selectedCurrencyCode = selectedCurrencyCode,
                                isGoogleCloudSyncActive = isGoogleSyncActive,
                                isAutoExpenseCalculationActive = isAutoCalculateActive,
                                isBankSyncActive = isBankSyncActive,
                                primaryLinkedBank = selectedBank,
                                monthlyBudgetLimit = monthlyBudget.toDouble(),
                                targetSavingsRatePercent = savingsTargetRate,
                                spendingAlertsEnabled = isSpendingAlertsActive,
                                weeklyReportEnabled = true
                            )
                            onComplete(finalProfile)
                        }
                        .padding(8.dp)
                        .testTag("onboarding_skip_button")
                )
            }

            // Smooth Progress Bar
            LinearProgressIndicator(
                progress = { currentStep / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PolishPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content Animated Transition
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "onboarding_step_transition"
                ) { step ->
                    when (step) {
                        1 -> StepWelcomeIdentity(
                            userName = userName,
                            onNameChange = { userName = it },
                            userEmail = userEmail,
                            onEmailChange = { userEmail = it },
                            selectedAvatar = selectedAvatarIndex,
                            onAvatarSelect = { selectedAvatarIndex = it }
                        )
                        2 -> StepLanguageAndCurrency(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelect = { selectedLanguage = it },
                            selectedCurrencyCode = selectedCurrencyCode,
                            onCurrencySelect = { code, symbol ->
                                selectedCurrencyCode = code
                                selectedCurrencySymbol = symbol
                            }
                        )
                        3 -> StepGoogleSyncAndCloud(
                            isGoogleSyncActive = isGoogleSyncActive,
                            onToggleGoogleSync = { isGoogleSyncActive = it },
                            userEmail = userEmail
                        )
                        4 -> StepBankSyncAndInstitutions(
                            isBankSyncActive = isBankSyncActive,
                            onToggleBankSync = { isBankSyncActive = it },
                            selectedBank = selectedBank,
                            onSelectBank = { selectedBank = it }
                        )
                        5 -> StepExpenseAutomationAndGoals(
                            isAutoCalculate = isAutoCalculateActive,
                            onToggleAutoCalculate = { isAutoCalculateActive = it },
                            isSpendingAlerts = isSpendingAlertsActive,
                            onToggleSpendingAlerts = { isSpendingAlertsActive = it },
                            monthlyBudget = monthlyBudget,
                            onBudgetChange = { monthlyBudget = it },
                            savingsTargetRate = savingsTargetRate,
                            onSavingsRateChange = { savingsTargetRate = it },
                            currencySymbol = selectedCurrencySymbol
                        )
                    }
                }
            }

            // Bottom Navigation CTA
            Button(
                onClick = {
                    if (currentStep < totalSteps) {
                        currentStep++
                    } else {
                        val finalProfile = UserProfileEntity(
                            id = 1,
                            isOnboardingCompleted = true,
                            userName = userName.ifBlank { "Jordan Walker" },
                            userEmail = userEmail.ifBlank { "jordan.spendwise@gmail.com" },
                            avatarIndex = selectedAvatarIndex,
                            selectedLanguage = selectedLanguage,
                            selectedCurrencySymbol = selectedCurrencySymbol,
                            selectedCurrencyCode = selectedCurrencyCode,
                            isGoogleCloudSyncActive = isGoogleSyncActive,
                            isAutoExpenseCalculationActive = isAutoCalculateActive,
                            isBankSyncActive = isBankSyncActive,
                            primaryLinkedBank = selectedBank,
                            monthlyBudgetLimit = monthlyBudget.toDouble(),
                            targetSavingsRatePercent = savingsTargetRate,
                            spendingAlertsEnabled = isSpendingAlertsActive,
                            weeklyReportEnabled = true
                        )
                        onComplete(finalProfile)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PolishPrimary,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (currentStep == totalSteps) "Launch SpendWise" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = if (currentStep == totalSteps) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// STEP 1: WELCOME & IDENTITY
// ------------------------------------------------------------------------------------------------
@Composable
private fun StepWelcomeIdentity(
    userName: String,
    onNameChange: (String) -> Unit,
    userEmail: String,
    onEmailChange: (String) -> Unit,
    selectedAvatar: Int,
    onAvatarSelect: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SpendWise Modern Monogram Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(PolishPrimary, PolishPrimaryDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to SpendWise",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Let's personalize your wealth & smart expense workspace",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Choose Profile Avatar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AVATAR_COLORS.forEachIndexed { index, color ->
                            val isSelected = selectedAvatar == index
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onAvatarSelect(index) }
                                    .testTag("avatar_choice_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = onNameChange,
                        label = { Text("Your Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_input_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = onEmailChange,
                        label = { Text("Email for Cloud Sync") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_input_email"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// STEP 2: LANGUAGE & CURRENCY
// ------------------------------------------------------------------------------------------------
@Composable
private fun StepLanguageAndCurrency(
    selectedLanguage: String,
    onLanguageSelect: (String) -> Unit,
    selectedCurrencyCode: String,
    onCurrencySelect: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Language & Currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aura adapts symbols and formats to your preferred locale",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Language Grid
        item {
            Text(
                text = "Select Language",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AVAILABLE_LANGUAGES.chunked(2).forEach { rowLanguages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowLanguages.forEach { lang ->
                            val isSelected = selectedLanguage == lang.name
                            Surface(
                                onClick = { onLanguageSelect(lang.name) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PolishPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PolishPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_select_${lang.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = lang.flag, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = lang.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Currency Selector
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AVAILABLE_CURRENCIES.forEach { curr ->
                    val isSelected = selectedCurrencyCode == curr.code
                    Surface(
                        onClick = { onCurrencySelect(curr.code, curr.symbol) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) PolishPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PolishPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("curr_select_${curr.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) PolishPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = curr.symbol,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = curr.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${curr.code} standard format",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = PolishPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// STEP 3: GOOGLE ACCOUNT & CLOUD SYNC
// ------------------------------------------------------------------------------------------------
@Composable
private fun StepGoogleSyncAndCloud(
    isGoogleSyncActive: Boolean,
    onToggleGoogleSync: (Boolean) -> Unit,
    userEmail: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Google Cloud Backup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Seamlessly back up your finances & restore across all your Android devices",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(PolishPrimary, PolishPrimaryDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = PolishTertiaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "256-BIT ENCRYPTION",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isGoogleSyncActive,
                                onCheckedChange = onToggleGoogleSync,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PolishPrimary,
                                    checkedTrackColor = PolishTertiaryGreen
                                ),
                                modifier = Modifier.testTag("onboarding_toggle_google_sync")
                            )
                        }

                        Text(
                            text = "Google Account Sync",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = if (isGoogleSyncActive)
                                "Connected: $userEmail\nAutomatic real-time snapshot every 15 minutes and end-to-end device encryption."
                            else
                                "Local-only storage active. Cloud backups and cross-device synchronization are paused.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureCheckRow(
                        icon = Icons.Default.CloudDone,
                        title = "Zero Data Loss Guarantee",
                        subtitle = "Your ledgers and receipts are saved even if you switch devices."
                    )
                    FeatureCheckRow(
                        icon = Icons.Default.Lock,
                        title = "Zero Knowledge Privacy",
                        subtitle = "Your banking numbers are hashed and never stored in plain text."
                    )
                    FeatureCheckRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Continuous Backup",
                        subtitle = "Changes instantly sync to your secure Google storage."
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// STEP 4: BANK LINKING & RECONCILIATION
// ------------------------------------------------------------------------------------------------
@Composable
private fun StepBankSyncAndInstitutions(
    isBankSyncActive: Boolean,
    onToggleBankSync: (Boolean) -> Unit,
    selectedBank: String,
    onSelectBank: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Bank Account & Feeds",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect live accounts for 1-tap reconciliation or keep an offline ledger",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automated Bank Feed Matching",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Auto-detect debit/credit alerts & cross-check local expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isBankSyncActive,
                        onCheckedChange = onToggleBankSync,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimary
                        ),
                        modifier = Modifier.testTag("onboarding_toggle_bank_feed")
                    )
                }
            }
        }

        item {
            Text(
                text = "Select Primary Institution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AVAILABLE_BANKS.forEach { bank ->
                    val isSelected = selectedBank == bank.name
                    Surface(
                        onClick = { onSelectBank(bank.name) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PolishPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) PolishPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_select_${bank.name.replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) PolishPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = bank.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = bank.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        if (bank.popular) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PolishGreenSuccess.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Instant Sync",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishGreenSuccess,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = bank.type,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = PolishPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// STEP 5: SMART EXPENSE AUTOMATION & GOALS
// ------------------------------------------------------------------------------------------------
@Composable
private fun StepExpenseAutomationAndGoals(
    isAutoCalculate: Boolean,
    onToggleAutoCalculate: (Boolean) -> Unit,
    isSpendingAlerts: Boolean,
    onToggleSpendingAlerts: (Boolean) -> Unit,
    monthlyBudget: Float,
    onBudgetChange: (Float) -> Unit,
    savingsTargetRate: Int,
    onSavingsRateChange: (Int) -> Unit,
    currencySymbol: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Expense Automation & Goals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aura automatically calculates daily burn rates, savings pace, and categories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Toggles
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = PolishPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Auto-Calculate Expenses & Tax",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Intelligent receipt sum calculations, tax breakdown, and tips",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAutoCalculate,
                            onCheckedChange = onToggleAutoCalculate,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PolishPrimary
                            ),
                            modifier = Modifier.testTag("onboarding_toggle_auto_calc")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = PolishPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Overspending Threshold Alerts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Receive prompt alerts when approaching 80% of category limits",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSpendingAlerts,
                            onCheckedChange = onToggleSpendingAlerts,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PolishPrimary
                            ),
                            modifier = Modifier.testTag("onboarding_toggle_alerts")
                        )
                    }
                }
            }
        }

        // Budget Limit Slider
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Target Budget",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$currencySymbol${"%,.0f".format(monthlyBudget)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PolishPrimary
                        )
                    }

                    Slider(
                        value = monthlyBudget,
                        onValueChange = onBudgetChange,
                        valueRange = 500f..10000f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = PolishPrimary,
                            activeTrackColor = PolishPrimary
                        ),
                        modifier = Modifier.testTag("onboarding_slider_budget")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${currencySymbol}500", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${currencySymbol}10,000", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Savings Target Slider
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Savings Rate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$savingsTargetRate%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PolishGreenSuccess
                        )
                    }

                    Slider(
                        value = savingsTargetRate.toFloat(),
                        onValueChange = { onSavingsRateChange(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = PolishGreenSuccess,
                            activeTrackColor = PolishGreenSuccess
                        ),
                        modifier = Modifier.testTag("onboarding_slider_savings")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "5% (Relaxed)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "60% (Aggressive)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = PolishPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PolishPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
