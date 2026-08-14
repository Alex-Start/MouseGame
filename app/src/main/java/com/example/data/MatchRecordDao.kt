package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRecordDao {
    @Query("SELECT * FROM match_history ORDER BY id DESC")
    fun getAllRecords(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MatchRecordEntity)

    @Query("DELETE FROM match_history")
    suspend fun clearAll()
}
