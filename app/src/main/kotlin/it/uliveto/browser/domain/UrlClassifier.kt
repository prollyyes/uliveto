package it.uliveto.browser.domain

import android.net.Uri

object UrlClassifier {
    fun buildNavigationUrl(input: String, engine: SearchEngine): String {
        val trimmed = input.trim()
        return if (looksLikeUrl(trimmed)) {
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed
            else "https://$trimmed"
        } else {
            engine.queryUrl.replace("%s", Uri.encode(trimmed))
        }
    }

    private fun looksLikeUrl(input: String): Boolean {
        if (input.startsWith("http://") || input.startsWith("https://")) return true
        val withoutProtocol = input.removePrefix("www.")
        val domainPattern = Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$")
        return domainPattern.matches(withoutProtocol)
    }
}
