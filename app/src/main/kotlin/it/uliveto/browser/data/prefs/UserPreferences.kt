package it.uliveto.browser.data.prefs

import it.uliveto.browser.domain.SearchEngine

data class UserPreferences(
    val searchEngine: SearchEngine = SearchEngine.DuckDuckGo,
    val userName: String = "",
    val customSearchEngineUrl: String = "",
    val theme: AppTheme = AppTheme.Terracotta,
    val navStyle: NavStyle = NavStyle.Hourglass,
    val safeBrowsingEnabled: Boolean = false,
)

enum class AppTheme { Terracotta, Aegean, OliveGrove, Amalfi, Santorini, Night, System }
enum class NavStyle { Hourglass, Classic }
