package it.uliveto.browser.engine

import android.util.Log
import org.mozilla.geckoview.GeckoRuntime

private const val TAG = "PrefsApplier"

/**
 * Applies a map of Mozilla preference key/value pairs to a [GeckoRuntime].
 *
 * GeckoView 128 does not expose a public batch pref-setter API. The package-private
 * [GeckoRuntime.setDefaultPrefs] dispatches a "GeckoView:SetDefaultPrefs" event that
 * Gecko handles on startup. We invoke it via reflection since we are in a different
 * package but the method exists in the compiled JAR.
 */
object PrefsApplier {

    fun apply(runtime: GeckoRuntime, prefs: Map<String, Any>) {
        if (prefs.isEmpty()) return

        try {
            val geckoBundle = buildGeckoBundle(prefs)
            val method = runtime.javaClass.getDeclaredMethod(
                "setDefaultPrefs",
                Class.forName("org.mozilla.gecko.util.GeckoBundle")
            )
            method.isAccessible = true
            method.invoke(runtime, geckoBundle)
            Log.i(TAG, "Applied ${prefs.size} Phoenix prefs to GeckoRuntime")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply Phoenix prefs via reflection: ${e.message}", e)
        }
    }

    /**
     * Builds a [org.mozilla.gecko.util.GeckoBundle] from the given map.
     * Accessed reflectively because GeckoBundle is in the internal gecko package.
     */
    private fun buildGeckoBundle(prefs: Map<String, Any>): Any {
        val bundleClass = Class.forName("org.mozilla.gecko.util.GeckoBundle")
        val bundle = bundleClass.getDeclaredConstructor(Int::class.java)
            .newInstance(prefs.size)

        val putBoolean = bundleClass.getMethod("putBoolean", String::class.java, Boolean::class.javaPrimitiveType)
        val putInt = bundleClass.getMethod("putInt", String::class.java, Int::class.javaPrimitiveType)
        val putString = bundleClass.getMethod("putString", String::class.java, String::class.java)

        for ((key, value) in prefs) {
            when (value) {
                is Boolean -> putBoolean.invoke(bundle, key, value)
                is Int -> putInt.invoke(bundle, key, value)
                is String -> putString.invoke(bundle, key, value)
            }
        }
        return bundle
    }
}
