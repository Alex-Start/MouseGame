package com.example.model

enum class WinMode(val displayName: String) {
    WIN_1("WIN 1"),
    WIN_ALL("WIN ALL")
}

enum class LaserMode(val displayName: String, val code: Int) {
    OFF("Off", 0),
    REFRESH("<<", 1),
    KILL("Kill", 2);

    fun next(): LaserMode = when (this) {
        OFF -> REFRESH
        REFRESH -> KILL
        KILL -> OFF
    }
}

data class GameSettings(
    val playerCount: Int = 4,
    val winningScore: Int = 10,
    val winMode: WinMode = WinMode.WIN_1,
    val laserMode: LaserMode = LaserMode.OFF,
    val fastCat: Boolean = true,
    val guesses: List<Int> = emptyList()
)
