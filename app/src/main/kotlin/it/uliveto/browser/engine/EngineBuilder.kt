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
 * M2 will add experimentDelegate and globalPrivacyControl via prefs.
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

        return GeckoRuntime.create(context, settings)
    }
}
