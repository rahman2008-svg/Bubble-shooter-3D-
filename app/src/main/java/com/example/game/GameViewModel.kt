package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HighScore
import com.example.data.HighScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HighScoreRepository(db.highScoreDao())

    // UI screen state mapping: "menu" | "game" | "scores" | "settings" | "credits"
    val navRoute = MutableStateFlow("menu")

    // High scores Reactive StateFlow
    val topHighScores: StateFlow<List<HighScore>> = repository.topHighScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Game preferences stored in memory for now
    val isSoundEnabled = MutableStateFlow(true)
    val isHapticEnabled = MutableStateFlow(true)
    val playerName = MutableStateFlow("Player 1")

    fun navigateTo(route: String) {
        navRoute.value = route
    }

    fun saveScore(score: Int, level: Int) {
        viewModelScope.launch {
            if (score > 0) {
                repository.insert(
                    HighScore(
                        score = score,
                        level = level,
                        playerName = playerName.value.trim().ifEmpty { "Player" }
                    )
                )
            }
        }
    }

    fun clearAllScores() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
