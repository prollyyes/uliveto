package it.uliveto.browser.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Generic [ViewModelProvider.Factory] that delegates creation to a lambda.
 *
 * Usage:
 * ```
 * val factory = ViewModelFactory { BrowserViewModel(container.geckoRuntime) }
 * val vm: BrowserViewModel by viewModels { factory }
 * ```
 */
class ViewModelFactory(
    private val creator: () -> ViewModel,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}
