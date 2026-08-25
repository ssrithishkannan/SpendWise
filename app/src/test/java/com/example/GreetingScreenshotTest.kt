package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.StreakHabitCard
import com.example.ui.screens.ProfessionalPolishHeroBalanceCard
import com.example.ui.theme.FinPulseTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun hero_balance_card_screenshot() {
        composeTestRule.setContent {
            FinPulseTheme {
                ProfessionalPolishHeroBalanceCard(
                    totalBalance = 4820.50,
                    monthlyIncome = 5400.0,
                    monthlyExpense = 1450.0,
                    monthlyLimit = 2000.0,
                    savingsRate = 0.73f,
                    isSyncing = false,
                    onReconciliationClick = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/hero_balance.png")
    }
}
