package it.uliveto.browser.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.uliveto.browser.domain.SearchEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPrefsRepository(context: Context) {
    private val dataStore = context.userPrefsDataStore

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPreferences(
                searchEngine = try {
                    SearchEngine.valueOf(prefs[Keys.SEARCH_ENGINE] ?: SearchEngine.DuckDuckGo.name)
                } catch (_: IllegalArgumentException) {
                    SearchEngine.DuckDuckGo
                },
                userName = prefs[Keys.USER_NAME] ?: "",
                customSearchEngineUrl = prefs[Keys.CUSTOM_SEARCH_ENGINE_URL] ?: "",
                theme = try {
                    AppTheme.valueOf(prefs[Keys.THEME] ?: AppTheme.Terracotta.name)
                } catch (_: IllegalArgumentException) {
                    AppTheme.Terracotta
                },
                navStyle = try {
                    NavStyle.valueOf(prefs[Keys.NAV_STYLE] ?: NavStyle.Hourglass.name)
                } catch (_: IllegalArgumentException) {
                    NavStyle.Hourglass
                },
                safeBrowsingEnabled = prefs[Keys.SAFE_BROWSING] ?: false,
            )
        }

    suspend fun setSearchEngine(engine: SearchEngine) =
        dataStore.edit { it[Keys.SEARCH_ENGINE] = engine.name }

    suspend fun setUserName(name: String) =
        dataStore.edit { it[Keys.USER_NAME] = name }

    suspend fun setCustomSearchEngineUrl(url: String) =
        dataStore.edit { it[Keys.CUSTOM_SEARCH_ENGINE_URL] = url }

    suspend fun setTheme(theme: AppTheme) =
        dataStore.edit { it[Keys.THEME] = theme.name }

    suspend fun setNavStyle(style: NavStyle) =
        dataStore.edit { it[Keys.NAV_STYLE] = style.name }

    suspend fun setSafeBrowsingEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.SAFE_BROWSING] = enabled }

    private object Keys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val USER_NAME = stringPreferencesKey("user_name")
        val CUSTOM_SEARCH_ENGINE_URL = stringPreferencesKey("custom_search_engine_url")
        val THEME = stringPreferencesKey("theme")
        val NAV_STYLE = stringPreferencesKey("nav_style")
        val SAFE_BROWSING = booleanPreferencesKey("safe_browsing")
    }
}

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
