package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("শিখি", appName)
  }

  @Test
  fun `license manager generates and validates deterministic keys`() {
    val key = com.example.data.local.LicenseManager.generateLicenseKey("01747570144_TRX123")
    org.junit.Assert.assertTrue(com.example.data.local.LicenseManager.isValidLicenseKey(key))
    org.junit.Assert.assertFalse(com.example.data.local.LicenseManager.isValidLicenseKey("PS-FAKE-0000"))
  }
}
