package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_questions")
data class CustomQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val category: String,
    val trueAnswer: String,
    val bluffHint: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_players")
data class SavedPlayerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val emoji: String,
    val colorHex: Long,
    val gamesPlayed: Int = 0,
    val totalWins: Int = 0
)
