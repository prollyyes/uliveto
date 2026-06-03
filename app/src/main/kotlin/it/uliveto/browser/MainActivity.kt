package it.uliveto.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import it.uliveto.browser.di.ViewModelFactory
import it.uliveto.browser.ui.UlivetoTheme
import it.uliveto.browser.ui.screens.browser.BrowserScreen
import it.uliveto.browser.ui.screens.browser.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val container get() = (application as UlivetoApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val browserVmFactory = ViewModelFactory { BrowserViewModel() }
        setContent {
            UlivetoTheme {
                BrowserScreen(runtime = container.geckoRuntime, vmFactory = browserVmFactory)
            }
        }
    }
}
