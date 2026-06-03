package it.uliveto.browser.data.prefs

import it.uliveto.browser.domain.SearchEngine

data class UserPreferences(
    val searchEngine: SearchEngine = SearchEngine.DuckDuckGo,
    val userName: String = "",
    val theme: AppTheme = AppTheme.Light,
    val navStyle: NavStyle = NavStyle.Hourglass,
    val safeBrowsingEnabled: Boolean = false,
)

enum class AppTheme { Light, Dark, OledBlack, FollowSystem }
enum class NavStyle { Hourglass, Classic }
