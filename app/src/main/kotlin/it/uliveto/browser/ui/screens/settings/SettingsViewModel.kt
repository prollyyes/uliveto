package it.uliveto.browser.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.data.prefs.NavStyle
import it.uliveto.browser.data.prefs.UserPreferences
import it.uliveto.browser.data.prefs.UserPrefsRepository
import it.uliveto.browser.domain.SearchEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: UserPrefsRepository) : ViewModel() {

    val preferences = repo.preferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserPreferences(),
    )

    fun setSearchEngine(e: SearchEngine) = viewModelScope.launch { repo.setSearchEngine(e) }
    fun setTheme(t: AppTheme) = viewModelScope.launch { repo.setTheme(t) }
    fun setNavStyle(n: NavStyle) = viewModelScope.launch { repo.setNavStyle(n) }
    fun setSafeBrowsing(b: Boolean) = viewModelScope.launch { repo.setSafeBrowsingEnabled(b) }
}
