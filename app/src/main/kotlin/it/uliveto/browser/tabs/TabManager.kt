package it.uliveto.browser.tabs

import androidx.compose.runtime.mutableStateListOf
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "about:blank",
    val title: String = "",
)

object TabManager {
    val tabs = mutableStateListOf<BrowserTab>()
    private var _activeTabId: String? = null
    val activeTabId: String? get() = _activeTabId

    private val sessions = mutableMapOf<String, GeckoSession>()

    // Tracks sessions whose initial loadUri() has not yet been fired.
    // loadUri must be called AFTER GeckoView.setSession() to ensure the view
    // is attached before content starts loading; otherwise GeckoView renders blank.
    private val pendingLoadIds = mutableSetOf<String>()

    fun createTab(url: String, runtime: GeckoRuntime): BrowserTab {
        val tab = BrowserTab(url = url)
        val session = buildSession()
        session.open(runtime)
        // Do NOT call session.loadUri() here — defer to BrowserScreen's DisposableEffect
        // so it fires after GeckoView.setSession() in the layout phase.
        sessions[tab.id] = session
        pendingLoadIds.add(tab.id)
        tabs.add(tab)
        _activeTabId = tab.id
        return tab
    }

    fun closeTab(id: String) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index == -1) return
        sessions[id]?.close()
        sessions.remove(id)
        pendingLoadIds.remove(id)
        tabs.removeAt(index)
        if (_activeTabId == id) _activeTabId = tabs.lastOrNull()?.id
    }

    fun getSession(tabId: String): GeckoSession? = sessions[tabId]

    fun setActiveTab(id: String) {
        if (tabs.any { it.id == id }) _activeTabId = id
    }

    fun updateTab(tabId: String, url: String? = null, title: String? = null) {
        val index = tabs.indexOfFirst { it.id == tabId }
        if (index == -1) return
        val tab = tabs[index]
        tabs[index] = tab.copy(url = url ?: tab.url, title = title ?: tab.title)
    }

    fun getTab(tabId: String): BrowserTab? = tabs.find { it.id == tabId }

    /**
     * Returns the initial URL to load for [tabId] on its first appearance in BrowserScreen,
     * or null if the session has already been initialised. Calling this a second time for
     * the same tab always returns null so the page is not reloaded on tab switch.
     */
    fun consumeInitialUrl(tabId: String): String? {
        if (!pendingLoadIds.remove(tabId)) return null
        return getTab(tabId)?.url?.takeIf { it.isNotBlank() && it != "about:blank" }
    }

    private fun buildSession(): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .build()
        return GeckoSession(settings)
    }
}
