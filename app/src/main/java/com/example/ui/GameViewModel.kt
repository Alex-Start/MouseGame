package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.model.GameEngine
import com.example.model.GameSettings
import com.example.model.LaserMode
import com.example.model.Player
import com.example.model.WinMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val id: Int,
    val name: String,
    val score: Int,
    val isAlive: Boolean,
    val winnerRank: Int?,
    val predictedRank: Int? = null,
    val keyPosition: Int? = null,
    val hasKey: Boolean = false,
    val isKeyCollected: Boolean = false,
    val isCaged: Boolean = false,
    val cheesePosition: Int? = null,
    val isCheeseCollected: Boolean = false,
    val trapPosition: Int? = null,
    val isTrapTriggered: Boolean = false,
    val stunnedTurns: Int = 0
)

data class GameUiState(
    val players: List<PlayerUiState> = emptyList(),
    val laserLineNumber: Int = 0,
    val laserLineTime: Int = 0,
    val laserLineTimeMax: Int = 10,
    val laserLineNumber2: Int = 0,
    val laserLineTime2: Int = 0,
    val laserLineTimeMax2: Int = 10,
    val isGameOver: Boolean = false,
    val durationFormatted: String = "00:00:00",
    val winMode: WinMode = WinMode.WIN_1,
    val laserMode: LaserMode = LaserMode.OFF,
    val hasCheese: Boolean = false,
    val hasTrap: Boolean = false,
    val sneakyCat: Boolean = false,
    val dualCat: Boolean = false,
    val countWinners: Int = 0,
    val countKilled: Int = 0,
    val winningScore: Int = 10,
    val winnerNames: String = "",
    val isMuted: Boolean = false,
    val guesses: List<Int> = emptyList(),
    val betAmount: Int = 50,
    val betType: com.example.model.BetType = com.example.model.BetType.EXACT_ORDER,
    val tick: Long = 0L
)

fun GameEngine.toUiState(isMuted: Boolean = false): GameUiState {
    val winnerList = winners.toMap()

    // Calculate complete rank map for ALL players who finished or are alive
    val nextRankStart = (winnerList.values.maxOrNull() ?: 0) + 1
    val unrankedAlivePlayers = players.filter { !winnerList.containsKey(it.id) && it.isAlive && !it.isCaged }
    val sortedUnrankedAlive = unrankedAlivePlayers.sortedWith(
        compareByDescending<Player> { it.score }
            .thenBy { it.id }
    )
    val fullRankMap = winnerList.toMutableMap()
    sortedUnrankedAlive.forEachIndexed { index, player ->
        fullRankMap[player.id] = nextRankStart + index
    }

    val winnerNamesStr = if (countWinners() > 0) {
        players
            .filter { winnerList.containsKey(it.id) }
            .sortedBy { winnerList[it.id] }
            .joinToString(", ") { "${it.name.trim()} (#${winnerList[it.id]})" }
    } else ""

    val guessList = settings.guesses

    return GameUiState(
        players = players.map { p ->
            val predictedIdx = guessList.indexOf(p.id)
            val predRank = if (predictedIdx >= 0) predictedIdx + 1 else null
            PlayerUiState(
                id = p.id,
                name = p.name,
                score = p.score,
                isAlive = p.isAlive,
                winnerRank = fullRankMap[p.id],
                predictedRank = predRank,
                keyPosition = p.keyPosition,
                hasKey = p.hasKey,
                isKeyCollected = p.isKeyCollected,
                isCaged = p.isCaged,
                cheesePosition = p.cheesePosition,
                isCheeseCollected = p.isCheeseCollected,
                trapPosition = p.trapPosition,
                isTrapTriggered = p.isTrapTriggered,
                stunnedTurns = p.stunnedTurns
            )
        },
        laserLineNumber = laserLineNumber,
        laserLineTime = laserLineTime,
        laserLineTimeMax = getEffectiveLaserTimeMax(),
        laserLineNumber2 = laserLineNumber2,
        laserLineTime2 = laserLineTime2,
        laserLineTimeMax2 = getEffectiveLaserTimeMax2(),
        isGameOver = isGameOver,
        durationFormatted = formatDuration(),
        winMode = settings.winMode,
        laserMode = settings.laserMode,
        hasCheese = settings.hasCheese,
        hasTrap = settings.hasTrap,
        sneakyCat = settings.sneakyCat,
        dualCat = settings.dualCat,
        countWinners = countWinners(),
        countKilled = countKilled(),
        winningScore = settings.winningScore,
        winnerNames = winnerNamesStr,
        isMuted = isMuted,
        guesses = guessList,
        betAmount = settings.betAmount,
        betType = settings.betType,
        tick = System.currentTimeMillis()
    )
}

sealed class GameState {
    object Menu : GameState()
    data class Playing(val uiState: GameUiState) : GameState()
}

class GameViewModel : ViewModel() {

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _matchHistory = MutableStateFlow<List<com.example.model.MatchRecord>>(emptyList())
    val matchHistory: StateFlow<List<com.example.model.MatchRecord>> = _matchHistory.asStateFlow()

    fun addMatchRecord(record: com.example.model.MatchRecord) {
        _matchHistory.value = listOf(record) + _matchHistory.value
    }

    fun clearMatchHistory() {
        _matchHistory.value = emptyList()
        soundManager.playButtonClick()
    }
    private val _gameState = MutableStateFlow<GameState>(GameState.Menu)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val soundManager = SoundManager()

    private var gameLoopJob: Job? = null
    private var isPaused: Boolean = false

    fun toggleMute() {
        soundManager.isMuted = !soundManager.isMuted
        soundManager.playButtonClick()
        val current = _gameState.value
        if (current is GameState.Playing) {
            _gameState.value = GameState.Playing(current.uiState.copy(isMuted = soundManager.isMuted))
        }
    }

    fun toggleGuess(playerId: Int) {
        val current = _settings.value
        val currentGuesses = current.guesses.toMutableList()
        if (currentGuesses.contains(playerId)) {
            currentGuesses.remove(playerId)
        } else {
            currentGuesses.add(playerId)
        }
        _settings.value = current.copy(guesses = currentGuesses)
        soundManager.playButtonClick()
    }

    fun updatePlayerCount(delta: Int) {
        val current = _settings.value
        val newCount = (current.playerCount + delta).coerceIn(1, 10)
        val validGuesses = current.guesses.filter { it <= newCount }
        _settings.value = current.copy(playerCount = newCount, guesses = validGuesses)
        soundManager.playButtonClick()
    }

    fun toggleLaserMode() {
        val current = _settings.value
        _settings.value = current.copy(laserMode = current.laserMode.next())
        soundManager.playSettingToggle()
    }

    fun setLaserMode(mode: LaserMode) {
        val current = _settings.value
        _settings.value = current.copy(laserMode = mode)
        soundManager.playSettingToggle()
    }

    fun updateWinningScore(delta: Int) {
        val current = _settings.value
        val newScore = (current.winningScore + delta).coerceAtLeast(1)
        _settings.value = current.copy(winningScore = newScore)
        soundManager.playButtonClick()
    }

    fun toggleFastCat() {
        val current = _settings.value
        _settings.value = current.copy(fastCat = !current.fastCat)
        soundManager.playSettingToggle()
    }

    fun toggleCheese() {
        val current = _settings.value
        _settings.value = current.copy(hasCheese = !current.hasCheese)
        soundManager.playSettingToggle()
    }

    fun toggleTrap() {
        val current = _settings.value
        _settings.value = current.copy(hasTrap = !current.hasTrap)
        soundManager.playSettingToggle()
    }

    fun toggleSneakyCat() {
        val current = _settings.value
        _settings.value = current.copy(sneakyCat = !current.sneakyCat)
        soundManager.playSettingToggle()
    }

    fun toggleDualCat() {
        val current = _settings.value
        _settings.value = current.copy(dualCat = !current.dualCat)
        soundManager.playSettingToggle()
    }

    fun setBetAmount(amount: Int) {
        val current = _settings.value
        _settings.value = current.copy(betAmount = amount)
        soundManager.playSettingToggle()
    }

    fun setBetType(betType: com.example.model.BetType) {
        val current = _settings.value
        _settings.value = current.copy(betType = betType)
        soundManager.playSettingToggle()
    }

    fun setWinMode(winMode: WinMode) {
        val current = _settings.value
        _settings.value = current.copy(winMode = winMode)
        soundManager.playButtonClick()
    }

    fun startGame(mode: WinMode) {
        val currentSettings = _settings.value.copy(winMode = mode)
        _settings.value = currentSettings
        val engine = GameEngine(currentSettings)
        _gameState.value = GameState.Playing(engine.toUiState(soundManager.isMuted))
        isPaused = false

        soundManager.playStartGame()
        startLoop(engine)
    }

    fun returnToMenu() {
        soundManager.playButtonClick()
        gameLoopJob?.cancel()
        gameLoopJob = null
        _gameState.value = GameState.Menu
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun restartGame() {
        gameLoopJob?.cancel()
        val currentSettings = _settings.value
        val engine = GameEngine(currentSettings)
        _gameState.value = GameState.Playing(engine.toUiState(soundManager.isMuted))
        isPaused = false

        soundManager.playStartGame()
        startLoop(engine)
    }

    private fun startLoop(engine: GameEngine) {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            delay(1000)
            var prevWinnersCount = 0
            var prevKilledCount = 0
            var prevKeysCollected = 0
            var prevCheeseCollected = 0
            var prevTrapsTriggered = 0

            while (!engine.isGameOver) {
                if (!isPaused) {
                    if (engine.settings.laserMode != LaserMode.OFF) {
                        engine.advanceLaserTime()
                        soundManager.playLaserTick()

                        engine.playRound()
                        soundManager.playMove()

                        val currentKeys = engine.players.count { it.isKeyCollected }
                        if (currentKeys > prevKeysCollected) {
                            soundManager.playKeyPickup()
                            prevKeysCollected = currentKeys
                        }

                        val currentCheese = engine.players.count { it.isCheeseCollected }
                        if (currentCheese > prevCheeseCollected) {
                            soundManager.playCheesePickup()
                            prevCheeseCollected = currentCheese
                        }

                        val currentTraps = engine.players.count { it.isTrapTriggered }
                        if (currentTraps > prevTrapsTriggered) {
                            soundManager.playTrapSnap()
                            prevTrapsTriggered = currentTraps
                        }

                        val newWinners = engine.countWinners()
                        if (newWinners > prevWinnersCount) {
                            soundManager.playVictory()
                            prevWinnersCount = newWinners
                        }

                        _gameState.value = GameState.Playing(engine.toUiState(soundManager.isMuted))
                        delay(400)

                        val playersBeforeLaser = engine.players.toList()
                        engine.applyLaserCheck()
                        val newKilled = engine.countKilled()

                        if (engine.settings.laserMode == LaserMode.KEY_CAGE) {
                            val newlyCaged = engine.players.count { it.isCaged } - playersBeforeLaser.count { it.isCaged }
                            val escapedWithKey = playersBeforeLaser.count { it.hasKey } - engine.players.count { it.hasKey }
                            if (newlyCaged > 0) {
                                soundManager.playCaged()
                            } else if (escapedWithKey > 0) {
                                soundManager.playKeyEscape()
                            } else if (engine.laserLineTime == engine.getEffectiveLaserTimeMax()) {
                                soundManager.playLaserStrike()
                            }
                        } else if (newKilled > prevKilledCount) {
                            if (engine.settings.laserMode == LaserMode.KILL) {
                                soundManager.playEliminated()
                            } else {
                                soundManager.playLaserStrike()
                            }
                            prevKilledCount = newKilled
                        } else if (engine.laserLineTime == engine.getEffectiveLaserTimeMax()) {
                            soundManager.playLaserStrike()
                        }

                        _gameState.value = GameState.Playing(engine.toUiState(soundManager.isMuted))
                        delay(400)
                    } else {
                        engine.playRound()
                        soundManager.playMove()

                        val currentCheese = engine.players.count { it.isCheeseCollected }
                        if (currentCheese > prevCheeseCollected) {
                            soundManager.playCheesePickup()
                            prevCheeseCollected = currentCheese
                        }

                        val currentTraps = engine.players.count { it.isTrapTriggered }
                        if (currentTraps > prevTrapsTriggered) {
                            soundManager.playTrapSnap()
                            prevTrapsTriggered = currentTraps
                        }

                        val newWinners = engine.countWinners()
                        if (newWinners > prevWinnersCount) {
                            soundManager.playVictory()
                            prevWinnersCount = newWinners
                        }

                        _gameState.value = GameState.Playing(engine.toUiState(soundManager.isMuted))
                        delay(600)
                    }
                } else {
                    delay(200)
                }
            }
            // Final emit
            _gameState.value = GameState.Playing(engine.toUiState(soundManager.isMuted))
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}

