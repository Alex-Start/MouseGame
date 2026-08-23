package com.example.model

enum class WinMode(val displayName: String) {
    WIN_1("WIN 1"),
    WIN_ALL("WIN ALL")
}

enum class LaserMode(val displayName: String, val code: Int) {
    OFF("Off", 0),
    REFRESH("<<", 1),
    KILL("Kill", 2),
    KEY_CAGE("Key & Cage", 3);

    fun next(): LaserMode = when (this) {
        OFF -> REFRESH
        REFRESH -> KILL
        KILL -> KEY_CAGE
        KEY_CAGE -> OFF
    }
}

enum class BetType(val displayName: String, val multiplier: Int) {
    ANY_POSITION("Any Match (2x)", 2),
    WINNER_ONLY("Winner (3x)", 3),
    EXACT_ORDER("Exact Order (5x)", 5)
}

data class MatchRecord(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val modeSummary: String,
    val predictionsSummary: String,
    val winnersSummary: String,
    val pointsEarned: Int
)

data class GameSettings(
    val playerCount: Int = 4,
    val winningScore: Int = 10,
    val winMode: WinMode = WinMode.WIN_1,
    val laserMode: LaserMode = LaserMode.OFF,
    val fastCat: Boolean = true,
    val guesses: List<Int> = emptyList(),
    val hasCheese: Boolean = false,
    val hasTrap: Boolean = false,
    val sneakyCat: Boolean = false,
    val dualCat: Boolean = false,
    val betAmount: Int = 50,
    val betType: BetType = BetType.EXACT_ORDER,
    val customNames: Map<Int, String> = emptyMap()
) {
    fun getPlayerName(playerId: Int): String {
        return customNames[playerId]?.trim()?.ifEmpty { null } ?: "Player $playerId"
    }
}

