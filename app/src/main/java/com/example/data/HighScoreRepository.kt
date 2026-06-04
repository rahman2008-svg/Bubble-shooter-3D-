package com.example.data

import kotlinx.coroutines.flow.Flow

class HighScoreRepository(private val highScoreDao: HighScoreDao) {
    val topHighScores: Flow<List<HighScore>> = highScoreDao.getTopHighScores()

    suspend fun insert(highScore: HighScore) {
        highScoreDao.insertHighScore(highScore)
    }

    suspend fun clearAll() {
        highScoreDao.clearAllHighScores()
    }
}
