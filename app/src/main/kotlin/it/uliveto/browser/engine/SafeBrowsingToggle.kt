package it.uliveto.browser.engine

import android.util.Log
import org.mozilla.geckoview.GeckoRuntime

private const val TAG = "SafeBrowsingToggle"

/**
 * Enables or disables Google Safe Browsing by setting the relevant Mozilla prefs.
 *
 * When disabled (privacy default), all SB check and URL prefs are zeroed out so
 * the engine makes no outbound requests to Google Safe Browsing endpoints.
 */
object SafeBrowsingToggle {

    private val SB_URLS = listOf(
        "browser.safebrowsing.provider.google4.updateURL",
        "browser.safebrowsing.provider.google4.gethashURL",
        "browser.safebrowsing.provider.google4.reportURL",
        "browser.safebrowsing.provider.google4.reportPhishMistakeURL",
        "browser.safebrowsing.provider.google4.reportMalwareMistakeURL",
        "browser.safebrowsing.provider.google4.advisoryURL",
        "browser.safebrowsing.provider.google4.dataSharingURL",
    )

    fun setEnabled(runtime: GeckoRuntime, enabled: Boolean) {
        val prefs = mutableMapOf<String, Any>(
            "browser.safebrowsing.malware.enabled" to enabled,
            "browser.safebrowsing.phishing.enabled" to enabled,
            "browser.safebrowsing.blockedURIs.enabled" to enabled,
            "browser.safebrowsing.downloads.enabled" to enabled,
        )
        if (!enabled) {
            SB_URLS.forEach { key -> prefs[key] = "" }
        }
        PrefsApplier.apply(runtime, prefs)
        Log.i(TAG, "Safe Browsing set to enabled=$enabled")
    }
}
