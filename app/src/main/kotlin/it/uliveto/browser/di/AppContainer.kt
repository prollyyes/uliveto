package it.uliveto.browser.di

import android.content.Context
import it.uliveto.browser.engine.EngineBuilder
import org.mozilla.geckoview.GeckoRuntime

/**
 * Manual DI container — holds app-scoped singletons.
 * In M1 this only contains the raw GeckoRuntime.
 * AC wrappers (BrowserEngine, BrowserStore, etc.) will be added in M2.
 */
class AppContainer(context: Context) {

    val geckoRuntime: GeckoRuntime = EngineBuilder.buildRuntime(context.applicationContext)
}
