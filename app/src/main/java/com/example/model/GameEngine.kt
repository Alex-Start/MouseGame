package com.example.model

import kotlin.math.max
import kotlin.math.min

class GameEngine(val settings: GameSettings) {
    var players: List<Player> = List(settings.playerCount) { i ->
        Player(id = i + 1, name = "Player ${i + 1}")
    }
        private set

    val winners = mutableMapOf<Int, Int>()
    private var lastPlace = 0

    var laserLineNumber: Int = 0
        private set
    var laserLineTime: Int = 0
        private set
    val killerLineTimeMax: Int = 10

    var isGameOver: Boolean = false
        private set

    val startTimeMs: Long = System.currentTimeMillis()
    var durationSeconds: Long = 0
        private set

    fun getEffectiveLaserTimeMax(): Int {
        return if (settings.fastCat) {
            val startRunFasterFraction = 5.0 / 6.0
            val step = max(1.0, (settings.winningScore * startRunFasterFraction) / killerLineTimeMax).toInt()
            val change = max(1, laserLineNumber / max(1, step))
            max(1, min(killerLineTimeMax, killerLineTimeMax - change + 1))
        } else {
            killerLineTimeMax
        }
    }

    fun playRound() {
        if (isGameOver) return

        updateDuration()

        val alivePlayersToMove = players.filter { it.isAlive && it.score < settings.winningScore && !winners.containsKey(it.id) }
        if (alivePlayersToMove.isNotEmpty()) {
            val choices = mutableListOf<String>()
            repeat(players.size) { choices.add("one+1") }
            choices.add("all+1")
            choices.add("all-1")

            when (choices.random()) {
                "one+1" -> {
                    val p = alivePlayersToMove.random()
                    updatePlayerScore(p.id, 1)
                }
                "all+1" -> {
                    players.forEach { updatePlayerScore(it.id, 1) }
                }
                "all-1" -> {
                    players.forEach { updatePlayerScore(it.id, -1) }
                }
            }
        }

        checkWinners()
        checkGameOver()
    }

    fun advanceLaserTime() {
        if (isGameOver) return

        updateDuration()

        laserLineTime++
        if (laserLineTime > getEffectiveLaserTimeMax()) {
            laserLineNumber++
            if (laserLineNumber >= settings.winningScore) {
                laserLineNumber = 1
            }
            laserLineTime = 0
        }
    }

    fun applyLaserCheck() {
        if (isGameOver) return
        if (laserLineTime != getEffectiveLaserTimeMax()) return
        if (laserLineNumber == 0) return

        players = players.map { player ->
            if (player.isAlive && player.score == laserLineNumber) {
                when (settings.laserMode) {
                    LaserMode.REFRESH -> player.copy(score = 0)
                    LaserMode.KILL -> player.copy(isAlive = false)
                    LaserMode.OFF -> player
                }
            } else {
                player
            }
        }

        checkGameOver()
    }

    private fun updatePlayerScore(playerId: Int, delta: Int) {
        if (winners.containsKey(playerId)) return
        players = players.map { p ->
            if (p.id == playerId && p.isAlive) {
                val newScore = (p.score + delta).coerceAtLeast(0)
                p.copy(score = newScore)
            } else {
                p
            }
        }
    }

    private fun checkWinners() {
        players.forEach { player ->
            if (player.score >= settings.winningScore && !winners.containsKey(player.id)) {
                lastPlace++
                winners[player.id] = lastPlace
            }
        }
    }

    private fun updateDuration() {
        durationSeconds = (System.currentTimeMillis() - startTimeMs) / 1000
    }

    fun countWinners(): Int = winners.size
    fun countKilled(): Int = players.count { !it.isAlive }

    private fun checkGameOver() {
        val total = players.size
        val wCount = countWinners()
        val kCount = countKilled()

        isGameOver = when (settings.winMode) {
            WinMode.WIN_1 -> {
                if (settings.laserMode == LaserMode.KILL) {
                    wCount > 0 || kCount == total
                } else {
                    wCount > 0
                }
            }
            WinMode.WIN_ALL -> {
                if (settings.laserMode == LaserMode.KILL) {
                    wCount + kCount == total
                } else {
                    wCount == total
                }
            }
        }
        if (isGameOver) {
            updateDuration()
        }
    }

    fun formatDuration(): String {
        val hrs = durationSeconds / 3600
        val mins = (durationSeconds % 3600) / 60
        val secs = durationSeconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }
}

