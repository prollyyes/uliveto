package it.uliveto.browser.ui.screens.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.data.prefs.NavStyle
import it.uliveto.browser.data.prefs.UserPreferences
import it.uliveto.browser.data.prefs.UserPrefsRepository
import it.uliveto.browser.domain.SearchEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StartViewModel(private val prefsRepository: UserPrefsRepository) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = prefsRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch { prefsRepository.setSearchEngine(engine) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { prefsRepository.setUserName(name) }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { prefsRepository.setTheme(theme) }
    }

    fun setNavStyle(style: NavStyle) {
        viewModelScope.launch { prefsRepository.setNavStyle(style) }
    }

    fun setSafeBrowsingEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setSafeBrowsingEnabled(enabled) }
    }

    companion object {
        // Process-scoped flag so the name dialog never re-appears on new-tab navigation
        @Volatile
        var namePromptShown = false
    }
}
