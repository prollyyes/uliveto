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

    fun createTab(url: String, runtime: GeckoRuntime): BrowserTab {
        val tab = BrowserTab(url = url)
        val session = buildSession()
        session.open(runtime)
        // Start the load immediately — GeckoSession resolves DNS, opens TCP and TLS,
        // and parses HTML entirely independently of whether a GeckoView is attached.
        // BrowserScreen only needs to call setSession() to wire up the compositor;
        // by then the network round-trip is already in flight.
        if (url.isNotBlank() && url != "about:blank") session.loadUri(url)
        sessions[tab.id] = session
        tabs.add(tab)
        _activeTabId = tab.id
        return tab
    }

    fun closeTab(id: String) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index == -1) return
        sessions[id]?.close()
        sessions.remove(id)
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

    private fun buildSession(): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(true)
            .build()
        return GeckoSession(settings)
    }
}
