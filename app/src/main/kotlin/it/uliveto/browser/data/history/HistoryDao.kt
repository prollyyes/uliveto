package it.uliveto.browser.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 100")
    fun getRecent(): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entry: HistoryEntity)
}
