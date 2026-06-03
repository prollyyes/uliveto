package it.uliveto.browser

import android.app.Application
import it.uliveto.browser.di.AppContainer

class UlivetoApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
