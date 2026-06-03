package it.uliveto.browser.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.uliveto.browser.domain.SearchEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPrefsRepository(private val context: Context) {

    private val dataStore = context.dataStore

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserPreferences(
                searchEngine = SearchEngine.valueOf(
                    prefs[PrefsKeys.SEARCH_ENGINE] ?: SearchEngine.DuckDuckGo.name
                ),
                userName = prefs[PrefsKeys.USER_NAME] ?: "",
            )
        }

    suspend fun setSearchEngine(engine: SearchEngine) {
        dataStore.edit { it[PrefsKeys.SEARCH_ENGINE] = engine.name }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[PrefsKeys.USER_NAME] = name }
    }

    private object PrefsKeys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val USER_NAME = stringPreferencesKey("user_name")
    }
}

// Extension property for DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
