package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SpendWise", appName)
  }

  @Test
  fun `user profile default state and onboarding toggle`() {
    val defaultProfile = UserProfileEntity()
    assertFalse(defaultProfile.isOnboardingCompleted)
    assertEquals("Jordan Walker", defaultProfile.userName)
    assertEquals("English", defaultProfile.selectedLanguage)
    assertEquals("$", defaultProfile.selectedCurrencySymbol)
    assertTrue(defaultProfile.isGoogleCloudSyncActive)
    assertTrue(defaultProfile.isAutoExpenseCalculationActive)

    val completed = defaultProfile.copy(isOnboardingCompleted = true, selectedLanguage = "Español")
    assertTrue(completed.isOnboardingCompleted)
    assertEquals("Español", completed.selectedLanguage)
  }
}
