package com.example.model

import kotlin.math.max
import kotlin.math.min

class GameEngine(val settings: GameSettings) {
    var players: List<Player> = List(settings.playerCount) { i ->
        val keyPos = if (settings.laserMode == LaserMode.KEY_CAGE) {
            val possible = (2 until settings.winningScore)
            if (possible.count() > 0) possible.random() else 2
        } else null
        val cheesePos = if (settings.hasCheese) {
            val possible = (2 until settings.winningScore).filter { it != keyPos }
            if (possible.isNotEmpty()) possible.random() else 2
        } else null
        val trapPos = if (settings.hasTrap) {
            val possible = (2 until settings.winningScore).filter { it != keyPos && it != cheesePos }
            if (possible.isNotEmpty()) possible.random() else 2
        } else null

        Player(
            id = i + 1,
            name = "Player ${i + 1}",
            keyPosition = keyPos,
            cheesePosition = cheesePos,
            trapPosition = trapPos
        )
    }
        private set

    val winners = mutableMapOf<Int, Int>()
    private var lastPlace = 0

    var laserLineNumber: Int = 0
        private set
    var laserLineTime: Int = 0
        private set
    val killerLineTimeMax: Int = 10

    // Dual Cat fields
    var laserLineNumber2: Int = if (settings.dualCat) (settings.winningScore / 2).coerceAtLeast(1) else 0
        private set
    var laserLineTime2: Int = 0
        private set

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

    fun getEffectiveLaserTimeMax2(): Int {
        val base = getEffectiveLaserTimeMax()
        return (base - 1).coerceAtLeast(4)
    }

    fun playRound() {
        if (isGameOver) return

        updateDuration()

        val alivePlayersToMove = players.filter { it.isAlive && !it.isCaged && it.score < settings.winningScore && !winners.containsKey(it.id) }
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

        // Cat 1
        laserLineTime++
        if (laserLineTime > getEffectiveLaserTimeMax()) {
            laserLineNumber++
            if (laserLineNumber >= settings.winningScore) {
                laserLineNumber = 1
            }
            laserLineTime = 0
        }

        // Cat 2 (Dual Cat)
        if (settings.dualCat) {
            laserLineTime2++
            if (laserLineTime2 > getEffectiveLaserTimeMax2()) {
                laserLineNumber2++
                if (laserLineNumber2 >= settings.winningScore) {
                    laserLineNumber2 = 1
                }
                laserLineTime2 = 0
            }
        }
    }

    fun applyLaserCheck() {
        if (isGameOver) return
        if (settings.laserMode == LaserMode.OFF) return

        val cat1Strike = laserLineTime == getEffectiveLaserTimeMax() && laserLineNumber > 0
        val cat2Strike = settings.dualCat && laserLineTime2 == getEffectiveLaserTimeMax2() && laserLineNumber2 > 0

        if (!cat1Strike && !cat2Strike) return

        players = players.map { player ->
            val hitByCat1 = cat1Strike && player.score == laserLineNumber
            val hitByCat2 = cat2Strike && player.score == laserLineNumber2

            if (player.isAlive && !player.isCaged && (hitByCat1 || hitByCat2)) {
                when (settings.laserMode) {
                    LaserMode.REFRESH -> player.copy(score = 0)
                    LaserMode.KILL -> player.copy(isAlive = false)
                    LaserMode.KEY_CAGE -> {
                        if (player.hasKey) {
                            // Free on start position, key consumed
                            player.copy(score = 0, hasKey = false)
                        } else {
                            // In cage on start position
                            player.copy(score = 0, isCaged = true, isAlive = false)
                        }
                    }
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
            if (p.id == playerId && p.isAlive && !p.isCaged) {
                if (p.stunnedTurns > 0) {
                    // Player is stunned by trap!
                    p.copy(stunnedTurns = p.stunnedTurns - 1)
                } else {
                    var newScore = (p.score + delta).coerceAtLeast(0)
                    var pKey = p.hasKey
                    var pKeyCollected = p.isKeyCollected
                    var pCheeseCollected = p.isCheeseCollected
                    var pTrapTriggered = p.isTrapTriggered
                    var pStunned = p.stunnedTurns

                    // Key Check
                    if (settings.laserMode == LaserMode.KEY_CAGE && p.keyPosition != null && !pKeyCollected) {
                        if (newScore >= p.keyPosition) {
                            pKey = true
                            pKeyCollected = true
                        }
                    }

                    // Cheese Check (+2 Speed Boost)
                    if (settings.hasCheese && p.cheesePosition != null && !pCheeseCollected) {
                        if (newScore >= p.cheesePosition) {
                            pCheeseCollected = true
                            newScore = min(settings.winningScore, newScore + 2)
                        }
                    }

                    // Trap Check (Stunned 1 turn)
                    if (settings.hasTrap && p.trapPosition != null && !pTrapTriggered) {
                        if (newScore == p.trapPosition) {
                            pTrapTriggered = true
                            pStunned = 1
                        }
                    }

                    p.copy(
                        score = newScore,
                        hasKey = pKey,
                        isKeyCollected = pKeyCollected,
                        isCheeseCollected = pCheeseCollected,
                        isTrapTriggered = pTrapTriggered,
                        stunnedTurns = pStunned
                    )
                }
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
    fun countKilled(): Int = players.count { !it.isAlive || it.isCaged }

    private fun checkGameOver() {
        val total = players.size
        val wCount = countWinners()
        val kCount = countKilled()

        isGameOver = when (settings.winMode) {
            WinMode.WIN_1 -> {
                if (settings.laserMode == LaserMode.KILL || settings.laserMode == LaserMode.KEY_CAGE) {
                    wCount > 0 || kCount == total
                } else {
                    wCount > 0
                }
            }
            WinMode.WIN_ALL -> {
                if (settings.laserMode == LaserMode.KILL || settings.laserMode == LaserMode.KEY_CAGE) {
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

