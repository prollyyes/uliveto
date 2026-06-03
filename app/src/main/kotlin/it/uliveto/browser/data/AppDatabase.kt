package it.uliveto.browser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.uliveto.browser.data.bookmarks.BookmarkEntity
import it.uliveto.browser.data.bookmarks.BookmarksDao
import it.uliveto.browser.data.history.HistoryDao
import it.uliveto.browser.data.history.HistoryEntity

@Database(entities = [BookmarkEntity::class, HistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarksDao(): BookmarksDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "uliveto.db")
                .build()
                .also { instance = it }
        }
    }
}
