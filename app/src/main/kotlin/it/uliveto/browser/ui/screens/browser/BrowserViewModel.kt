package it.uliveto.browser.ui.screens.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.uliveto.browser.data.bookmarks.BookmarksRepository
import kotlinx.coroutines.launch

class BrowserViewModel(private val bookmarksRepo: BookmarksRepository) : ViewModel() {

    fun saveBookmark(url: String, title: String) = viewModelScope.launch {
        bookmarksRepo.add(url, title)
    }
}
