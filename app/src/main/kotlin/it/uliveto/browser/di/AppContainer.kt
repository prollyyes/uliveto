package it.uliveto.browser.di

import android.content.Context
import it.uliveto.browser.data.prefs.UserPrefsRepository
import it.uliveto.browser.engine.EngineBuilder
import org.mozilla.geckoview.GeckoRuntime

/**
 * Manual DI container — holds app-scoped singletons.
 */
class AppContainer(context: Context) {

    val geckoRuntime: GeckoRuntime = EngineBuilder.getOrCreate(context.applicationContext)
    val userPrefsRepository: UserPrefsRepository = UserPrefsRepository(context.applicationContext)
}
