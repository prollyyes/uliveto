package it.uliveto.browser.data.bookmarks

import kotlinx.coroutines.flow.Flow

class BookmarksRepository(private val dao: BookmarksDao) {
    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAll()

    suspend fun add(url: String, title: String) = dao.insert(BookmarkEntity(url = url, title = title))

    suspend fun delete(bookmark: BookmarkEntity) = dao.delete(bookmark)
}
