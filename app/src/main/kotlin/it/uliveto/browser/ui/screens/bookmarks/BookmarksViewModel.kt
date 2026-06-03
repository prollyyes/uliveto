package it.uliveto.browser.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.uliveto.browser.data.bookmarks.BookmarkEntity
import it.uliveto.browser.data.bookmarks.BookmarksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(private val repo: BookmarksRepository) : ViewModel() {

    val bookmarks = repo.bookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun delete(bookmark: BookmarkEntity) = viewModelScope.launch {
        repo.delete(bookmark)
    }
}
