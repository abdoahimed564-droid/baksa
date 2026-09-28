package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM custom_questions ORDER BY id DESC")
    fun getAllCustomQuestions(): Flow<List<CustomQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomQuestion(question: CustomQuestionEntity): Long

    @Query("DELETE FROM custom_questions WHERE id = :id")
    suspend fun deleteCustomQuestion(id: Int)

    @Query("SELECT * FROM saved_players ORDER BY gamesPlayed DESC")
    fun getAllSavedPlayers(): Flow<List<SavedPlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayer(player: SavedPlayerEntity)

    @Query("DELETE FROM saved_players WHERE id = :id")
    suspend fun deletePlayer(id: String)
}
