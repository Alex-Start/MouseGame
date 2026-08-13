package com.example.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaserMode
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    onRecordMatch: (com.example.model.MatchRecord) -> Unit = {},
    onReturnToMenu: () -> Unit,
    onRestartGame: () -> Unit,
    onToggleMute: () -> Unit = {}
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    var hasRecordedHistory by remember { mutableStateOf(false) }

    val totalScoreSteps = uiState.winningScore
    val trackWidthDp = (totalScoreSteps * 40 + 220).dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14141E))
    ) {
        // Top Bar Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mouse Game Scoreboard",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(
                        containerColor = Color(0xFF2E2E42),
                        contentColor = Color(0xFFFFB74D)
                    ) {
                        Text(text = uiState.winMode.displayName, modifier = Modifier.padding(2.dp))
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onReturnToMenu,
                    modifier = Modifier.testTag("menu_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.testTag("mute_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Mute",
                        tint = if (uiState.isMuted) Color.Red else Color.Green
                    )
                }
                IconButton(
                    onClick = onRestartGame,
                    modifier = Modifier.testTag("restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart Game", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2C))
        )

        // Status Header Bar (Timer, Winners, Kills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222232))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF81D4FA), modifier = Modifier.size(18.dp))
                Text(
                    text = uiState.durationFormatted,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Winners: ${uiState.countWinners}",
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (uiState.laserMode == LaserMode.KILL) {
                    Text(
                        text = "Killed: ${uiState.countKilled}",
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Game Winner Banner
        if (uiState.countWinners > 0 && uiState.winnerNames.isNotEmpty()) {
            Surface(
                color = Color(0xFF262633),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏆 WINNER: ${uiState.winnerNames}! 🏆",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (uiState.guesses.isNotEmpty()) {
                        val correctGuesses = uiState.players.count { p ->
                            p.isAlive && !p.isCaged && p.predictedRank != null && p.winnerRank != null && p.winnerRank == p.predictedRank
                        }

                        val canEvaluateBet = uiState.isGameOver || uiState.countWinners > 0

                        if (canEvaluateBet) {
                            val predictedWinnerId = uiState.guesses.firstOrNull()
                            val isWinnerBetWon = predictedWinnerId != null &&
                                uiState.players.any { p -> p.id == predictedWinnerId && p.isAlive && !p.isCaged && p.winnerRank == 1 }

                            val hasAnyMatch = uiState.players.any { p ->
                                p.isAlive && !p.isCaged && p.predictedRank != null && p.winnerRank != null && p.winnerRank == p.predictedRank
                            }

                            val predictedPlayers = uiState.players.filter { it.predictedRank != null }
                            val isExactOrderWon = predictedPlayers.isNotEmpty() &&
                                predictedPlayers.all { p -> p.isAlive && !p.isCaged && p.winnerRank != null && p.winnerRank == p.predictedRank }

                            val isBetWon = when (uiState.betType) {
                                com.example.model.BetType.WINNER_ONLY -> isWinnerBetWon
                                com.example.model.BetType.ANY_POSITION -> hasAnyMatch
                                com.example.model.BetType.EXACT_ORDER -> isExactOrderWon
                            }

                            val payout = if (isBetWon) uiState.betAmount * uiState.betType.multiplier else -uiState.betAmount
                            val payoutText = if (payout >= 0) "💰 Bet Won: +$payout Pts!" else "💸 Bet Lost: -${uiState.betAmount} Pts"
                            val payoutColor = if (payout >= 0) Color(0xFF00E676) else Color(0xFFFF7043)

                            LaunchedEffect(canEvaluateBet) {
                                if (!hasRecordedHistory) {
                                    hasRecordedHistory = true
                                    val timeStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

                                    val laserStr = if (uiState.laserMode != com.example.model.LaserMode.OFF) "⚡ Laser: ${uiState.laserMode.displayName}" else "⚡ No Laser"

                                    val extrasList = mutableListOf<String>()
                                    if (uiState.hasCheese) extrasList.add("🧀 Cheese")
                                    if (uiState.hasTrap) extrasList.add("🪤 Trap")
                                    if (uiState.sneakyCat) extrasList.add("🐱 Sneaky Cat")
                                    if (uiState.dualCat) extrasList.add("🐱🐱 Dual Cat")
                                    val extrasStr = if (extrasList.isNotEmpty()) " • " + extrasList.joinToString(", ") else ""

                                    val betStr = "🎲 Bet: ${uiState.betType.displayName} (${uiState.betAmount} pts)"

                                    val modeStr = "${uiState.players.size} Players • ${uiState.winMode.displayName} • $laserStr$extrasStr • $betStr"

                                    val predStr = uiState.players.filter { it.predictedRank != null }
                                        .sortedBy { it.predictedRank }
                                        .joinToString(", ") { "P${it.id}: #${it.predictedRank}" }

                                    val actualStr = uiState.players
                                        .sortedWith(compareBy<PlayerUiState> { if (!it.isAlive || it.isCaged) 999 else (it.winnerRank ?: 900) }.thenBy { it.id })
                                        .joinToString(", ") { p ->
                                            if (!p.isAlive) "P${p.id}: K.O."
                                            else if (p.isCaged) "P${p.id}: Caged"
                                            else "P${p.id}: #${p.winnerRank ?: "-"}"
                                        }

                                    onRecordMatch(
                                        com.example.model.MatchRecord(
                                            timestamp = timeStr,
                                            modeSummary = modeStr,
                                            predictionsSummary = if (predStr.isEmpty()) "None" else predStr,
                                            winnersSummary = if (actualStr.isEmpty()) "None" else actualStr,
                                            pointsEarned = payout
                                        )
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "🎯 Predictions: $correctGuesses / ${uiState.guesses.size} Correct!",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = payoutText,
                                    color = payoutColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Text(
                                text = "⏳ Race in Progress... ($correctGuesses Correct So Far)",
                                color = Color(0xFF81D4FA),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Game Over Banner
        if (uiState.isGameOver) {
            Surface(
                color = Color(0xFF388E3C),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏁 GAME OVER 🏁",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Main Racing Track View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.width(trackWidthDp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Players List Track
                uiState.players.forEach { player ->
                    PlayerRowTrack(
                        player = player,
                        winningScore = uiState.winningScore
                    )
                }

                // Laser / Cat Indicator Row
                if (uiState.laserMode != LaserMode.OFF) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LaserCatRowTrack(
                        uiState = uiState
                    )
                }
            }
        }

        // Bottom Action Controls
        Surface(
            color = Color(0xFF1E1E2C),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReturnToMenu,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.testTag("menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Game Menu")
                }

                Button(
                    onClick = onRestartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043)),
                    modifier = Modifier.testTag("play_again_button")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.isGameOver) "Play Again" else "Restart")
                }
            }
        }
    }
}

@Composable
private fun PlayerRowTrack(
    player: PlayerUiState,
    winningScore: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2A))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player Label
        Row(
            modifier = Modifier.width(135.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (player.isAlive) Color(0xFF3949AB) else Color(0xFF424242),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.id}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${player.name.trim()}: ${player.score}",
                        color = if (player.isAlive && !player.isCaged) Color.White else Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textDecoration = if (!player.isAlive || player.isCaged) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (player.hasKey) {
                        Text(
                            text = "🔑",
                            fontSize = 12.sp
                        )
                    }
                }

                if (player.isCaged) {
                    Text(
                        text = "🔒 Caged",
                        color = Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (player.stunnedTurns > 0) {
                    Text(
                        text = "💫 Stunned",
                        color = Color(0xFFFF7043),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (player.predictedRank != null) {
                    val isCorrect = player.winnerRank != null && player.winnerRank == player.predictedRank
                    val tagText = if (isCorrect) "🎯 #${player.predictedRank} ✅" else "🎯 Pred #${player.predictedRank}"
                    val tagColor = if (isCorrect) Color(0xFF81C784) else Color(0xFFFFB74D)

                    Text(
                        text = tagText,
                        color = tagColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Track Lane
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track Line Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF2C2C3E), RoundedCornerShape(2.dp))
            )

            // Step dots along track
            for (step in 1..winningScore) {
                val stepFraction = step.toFloat() / winningScore.toFloat()
                val dotOffset = (maxWidth - 36.dp) * stepFraction
                Box(
                    modifier = Modifier
                        .offset(x = dotOffset)
                        .size(3.dp)
                        .background(Color(0xFF4A4A62), CircleShape)
                )
            }

            // Key on Track (if not collected yet)
            if (player.keyPosition != null && !player.isKeyCollected) {
                val keyFraction = (player.keyPosition.toFloat() / winningScore.toFloat()).coerceIn(0f, 1f)
                val keyOffset = (maxWidth - 36.dp) * keyFraction
                Box(
                    modifier = Modifier
                        .offset(x = keyOffset)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔑",
                        fontSize = 13.sp
                    )
                }
            }

            // Cheese on Track (if not collected yet)
            if (player.cheesePosition != null && !player.isCheeseCollected) {
                val cheeseFraction = (player.cheesePosition.toFloat() / winningScore.toFloat()).coerceIn(0f, 1f)
                val cheeseOffset = (maxWidth - 36.dp) * cheeseFraction
                Box(
                    modifier = Modifier
                        .offset(x = cheeseOffset)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧀",
                        fontSize = 13.sp
                    )
                }
            }

            // Trap on Track (if not triggered yet)
            if (player.trapPosition != null && !player.isTrapTriggered) {
                val trapFraction = (player.trapPosition.toFloat() / winningScore.toFloat()).coerceIn(0f, 1f)
                val trapOffset = (maxWidth - 36.dp) * trapFraction
                Box(
                    modifier = Modifier
                        .offset(x = trapOffset)
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🪤",
                        fontSize = 13.sp
                    )
                }
            }

            // Finish Line Marker
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF66BB6A))
            )

            // Animated Mouse / Cage Position
            val totalTrackWidth = maxWidth - 36.dp
            val positionFraction = (player.score.toFloat() / winningScore.toFloat()).coerceIn(0f, 1f)
            val targetMouseOffset = totalTrackWidth * positionFraction
            val animatedMouseOffset by animateDpAsState(
                targetValue = targetMouseOffset,
                animationSpec = tween(durationMillis = 300),
                label = "MouseOffset"
            )

            Box(
                modifier = Modifier.offset(x = animatedMouseOffset)
            ) {
                when {
                    player.isCaged -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(2.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                .background(Color(0x992E1C12), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔒", fontSize = 14.sp)
                        }
                    }
                    player.isAlive -> {
                        Box {
                            Image(
                                painter = painterResource(id = R.drawable.ic_mouse),
                                contentDescription = "Mouse",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            if (player.hasKey) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    Text("🔑", fontSize = 10.sp)
                                }
                            }
                            if (player.stunnedTurns > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                ) {
                                    Text("💫", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp, 20.dp)
                                .background(Color.Red, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💀", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Winner Rank Tag
            if (player.winnerRank != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEF5350))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${player.winnerRank}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LaserCatRowTrack(
    uiState: GameUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A1A24))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(130.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.PrecisionManufacturing,
                contentDescription = null,
                tint = Color(0xFFFF7043),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (uiState.dualCat) "Dual Cats" else if (uiState.sneakyCat) "Sneaky Cat" else "Laser Cat",
                color = Color(0xFFFF7043),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF3E2028), RoundedCornerShape(2.dp))
            )

            // Cat 1 Indicator
            if (uiState.laserLineNumber > 0) {
                val totalTrackWidth = maxWidth - 40.dp
                val positionFraction = (uiState.laserLineNumber.toFloat() / uiState.winningScore.toFloat()).coerceIn(0f, 1f)
                val targetCatOffset = totalTrackWidth * positionFraction
                val animatedCatOffset by animateDpAsState(
                    targetValue = targetCatOffset,
                    animationSpec = tween(durationMillis = 300),
                    label = "CatOffset"
                )

                Box(
                    modifier = Modifier.offset(x = animatedCatOffset)
                ) {
                    if (uiState.laserLineTime < uiState.laserLineTimeMax) {
                        // Countdown state
                        val isWarning = uiState.laserLineTime == uiState.laserLineTimeMax - 1
                        val displayText = if (uiState.sneakyCat) "❓" else "${uiState.laserLineTime}"
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isWarning) Color(0xFFE53935) else Color(0xFF424242)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Attack state (Cat Image + Red Laser Line)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(Color.Red)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_cat),
                                contentDescription = "Laser Cat 1",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }

            // Cat 2 Indicator (Dual Cat)
            if (uiState.dualCat && uiState.laserLineNumber2 > 0) {
                val totalTrackWidth = maxWidth - 40.dp
                val positionFraction2 = (uiState.laserLineNumber2.toFloat() / uiState.winningScore.toFloat()).coerceIn(0f, 1f)
                val targetCatOffset2 = totalTrackWidth * positionFraction2
                val animatedCatOffset2 by animateDpAsState(
                    targetValue = targetCatOffset2,
                    animationSpec = tween(durationMillis = 300),
                    label = "CatOffset2"
                )

                Box(
                    modifier = Modifier.offset(x = animatedCatOffset2)
                ) {
                    if (uiState.laserLineTime2 < uiState.laserLineTimeMax2) {
                        // Countdown state for Cat 2
                        val isWarning2 = uiState.laserLineTime2 == uiState.laserLineTimeMax2 - 1
                        val displayText2 = if (uiState.sneakyCat) "❓" else "${uiState.laserLineTime2}"
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isWarning2) Color(0xFFFF9800) else Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayText2,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Attack state for Cat 2 (Cyan Laser + Cat Image)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(Color(0xFF00E676))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_cat),
                                contentDescription = "Laser Cat 2",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

