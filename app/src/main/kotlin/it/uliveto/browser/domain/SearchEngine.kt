package it.uliveto.browser.domain

enum class SearchEngine(val displayName: String, val queryUrl: String) {
    DuckDuckGo("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
    Startpage("Startpage", "https://www.startpage.com/search?q=%s"),
    BraveSearch("Brave Search", "https://search.brave.com/search?q=%s"),
    Mojeek("Mojeek", "https://www.mojeek.com/search?q=%s"),
    Custom("Custom", ""),
}
