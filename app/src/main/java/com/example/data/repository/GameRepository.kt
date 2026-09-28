package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CustomQuestionEntity
import com.example.data.local.SavedPlayerEntity
import com.example.data.model.Player
import com.example.data.model.Question
import com.example.data.model.QuestionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val database: AppDatabase) {

    private val allBuiltInQuestions by lazy { QuestionsBank.getAllQuestions() }

    fun getQuestions(categories: Set<QuestionCategory>): List<Question> {
        val filtered = if (categories.contains(QuestionCategory.ALL) || categories.isEmpty()) {
            allBuiltInQuestions
        } else {
            allBuiltInQuestions.filter { categories.contains(it.category) }
        }
        return filtered.shuffled()
    }

    fun getAllCustomQuestions(): Flow<List<Question>> {
        return database.gameDao().getAllCustomQuestions().map { entities ->
            entities.map { entity ->
                Question(
                    id = 10000 + entity.id,
                    text = entity.text,
                    category = QuestionCategory.CUSTOM,
                    trueAnswer = entity.trueAnswer,
                    bluffHint = entity.bluffHint,
                    isCustom = true
                )
            }
        }
    }

    suspend fun addCustomQuestion(
        text: String,
        trueAnswer: String,
        bluffHint: String
    ): Long {
        val entity = CustomQuestionEntity(
            text = text,
            category = QuestionCategory.CUSTOM.name,
            trueAnswer = trueAnswer,
            bluffHint = bluffHint
        )
        return database.gameDao().insertCustomQuestion(entity)
    }

    suspend fun deleteCustomQuestion(id: Int) {
        val dbId = if (id >= 10000) id - 10000 else id
        database.gameDao().deleteCustomQuestion(dbId)
    }

    fun getSavedPlayers(): Flow<List<Player>> {
        return database.gameDao().getAllSavedPlayers().map { entities ->
            entities.map { entity ->
                Player(
                    id = entity.id,
                    name = entity.name,
                    emoji = entity.emoji,
                    colorHex = entity.colorHex
                )
            }
        }
    }

    suspend fun savePlayer(player: Player) {
        val entity = SavedPlayerEntity(
            id = player.id,
            name = player.name,
            emoji = player.emoji,
            colorHex = player.colorHex,
            gamesPlayed = 1,
            totalWins = if (player.score > 0) 1 else 0
        )
        database.gameDao().savePlayer(entity)
    }

    suspend fun deleteSavedPlayer(id: String) {
        database.gameDao().deletePlayer(id)
    }
}
