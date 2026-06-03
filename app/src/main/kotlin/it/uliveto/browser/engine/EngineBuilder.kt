package it.uliveto.browser.engine

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

/**
 * Builds a hardened [GeckoRuntime] singleton.
 *
 * Knobs applied in M1:
 * - Crash reporting disabled
 * - Remote debugging disabled
 * - about:config disabled
 *
 * Knobs applied in M2:
 * - Phoenix privacy prefs bundle applied via PrefsApplier
 * - Google Safe Browsing disabled (privacy default)
 *
 * GeckoRuntime.create() may only be called once per process; [getOrCreate] enforces that.
 */
object EngineBuilder {

    @Volatile
    private var instance: GeckoRuntime? = null

    fun getOrCreate(context: Context): GeckoRuntime {
        return instance ?: synchronized(this) {
            instance ?: buildRuntime(context.applicationContext).also { instance = it }
        }
    }

    private fun buildRuntime(context: Context): GeckoRuntime {
        // Disable Mozilla crash reporter before runtime init
        System.setProperty("MOZ_CRASHREPORTER_DISABLE", "1")

        val settings = GeckoRuntimeSettings.Builder()
            .crashHandler(null)          // disable crash reporting
            .remoteDebuggingEnabled(false)
            .aboutConfigEnabled(false)
            .build()

        val runtime = GeckoRuntime.create(context, settings)

        // Apply Phoenix prefs bundle (telemetry lockdown, privacy hardening)
        val prefs = PrefsLoader.load(context)
        PrefsApplier.apply(runtime, prefs)

        // Privacy default: disable Safe Browsing (no outbound Google SB requests)
        SafeBrowsingToggle.setEnabled(runtime, enabled = false)

        return runtime
    }
}
