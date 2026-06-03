package it.uliveto.browser

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that each pref key loaded from phoenix.android.js is applied to the GeckoRuntime.
 * Full assertion requires GeckoRuntime pref read-back API — wired in M2 completion.
 */
@RunWith(AndroidJUnit4::class)
class PhoenixPrefsAppliedTest {
    @Test
    fun phoenixPrefsFileLoadsWithoutError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = it.uliveto.browser.engine.PrefsLoader.load(context)
        assertTrue("Phoenix prefs bundle must contain at least 100 entries", prefs.size >= 100)
    }

    @Test
    fun knownTelemetryPrefsArePresent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = it.uliveto.browser.engine.PrefsLoader.load(context)

        val requiredKeys = listOf(
            "toolkit.telemetry.enabled",
            "datareporting.healthreport.uploadEnabled",
            "app.normandy.enabled",
            "network.captive-portal-service.enabled",
        )
        for (key in requiredKeys) {
            assertTrue("Expected pref '$key' to be in phoenix bundle", prefs.containsKey(key))
        }
    }

    @Test
    fun knownTelemetryPrefsAreDisabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = it.uliveto.browser.engine.PrefsLoader.load(context)

        val expectedFalse = listOf(
            "toolkit.telemetry.enabled",
            "toolkit.telemetry.unified",
            "datareporting.healthreport.uploadEnabled",
            "network.captive-portal-service.enabled",
            "network.connectivity-service.enabled",
        )
        for (key in expectedFalse) {
            val value = prefs[key]
            assertTrue(
                "Expected pref '$key' to be false, got: $value",
                value == false || value == 0
            )
        }
    }
}
