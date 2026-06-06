package it.uliveto.browser.domain

import android.net.Uri

private val DOMAIN_PATTERN = Regex("""^[a-zA-Z0-9.-]+(:\d+)?([/?#].*)?$""")

object UrlClassifier {
    fun buildNavigationUrl(input: String, engine: SearchEngine, customUrl: String = ""): String {
        val trimmed = input.trim()
        return if (looksLikeUrl(trimmed)) {
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed
            else "https://$trimmed"
        } else {
            val template = when {
                engine == SearchEngine.Custom && customUrl.isNotBlank() -> customUrl
                engine == SearchEngine.Custom -> SearchEngine.DuckDuckGo.queryUrl
                else -> engine.queryUrl
            }
            template.replace("%s", Uri.encode(trimmed))
        }
    }

    private fun looksLikeUrl(input: String): Boolean {
        if (input.startsWith("http://") || input.startsWith("https://")) return true
        if (!input.contains('.')) return false
        return DOMAIN_PATTERN.matches(input) &&
            android.util.Patterns.WEB_URL.matcher("https://$input").matches()
    }
}
