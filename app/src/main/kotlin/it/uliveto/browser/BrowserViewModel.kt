package it.uliveto.browser

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds transient UI state for [BrowserScreen].
 *
 * In M1 this is minimal — just the URL bar text.
 * Session / tab management moves to AC BrowserStore in M2.
 */
class BrowserViewModel : ViewModel() {

    private val _urlText = MutableStateFlow("")
    val urlText: StateFlow<String> = _urlText.asStateFlow()

    fun onUrlTextChanged(value: String) {
        _urlText.value = value
    }
}
