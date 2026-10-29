package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaserMode
import com.example.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    onStartRace: () -> Unit = {},
    onRenamePlayer: (Int, String) -> Unit = { _, _ -> },
    onRecordMatch: (com.example.model.MatchRecord) -> Unit = {},
    onReturnToMenu: () -> Unit,
    onRestartGame: () -> Unit,
    onToggleMute: () -> Unit = {}
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    var hasRecordedHistory by remember { mutableStateOf(false) }
    var renamingPlayer by remember { mutableStateOf<PlayerUiState?>(null) }
    var parentLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var firstRowTopPx by remember { mutableFloatStateOf(0f) }
    var cat1CenterXPx by remember { mutableFloatStateOf(0f) }
    var cat1TopYPx by remember { mutableFloatStateOf(0f) }
    var cat2CenterXPx by remember { mutableFloatStateOf(0f) }
    var cat2TopYPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(uiState.isGameOver) {
        if (!uiState.isGameOver) {
            hasRecordedHistory = false
        }
    }

    if (renamingPlayer != null) {
        RenamePlayerDialog(
            player = renamingPlayer!!,
            onConfirm = { newName ->
                onRenamePlayer(renamingPlayer!!.id, newName)
                renamingPlayer = null
            },
            onDismiss = {
                renamingPlayer = null
            }
        )
    }

    val totalScoreSteps = uiState.winningScore
    val trackWidthDp = (totalScoreSteps * 40 + 220).dp

    Box(modifier = Modifier.fillMaxSize()) {
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
                if (!uiState.isGameStarted) {
                    IconButton(
                        onClick = onStartRace,
                        modifier = Modifier.testTag("top_start_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Game", tint = Color(0xFF00E676))
                    }
                } else {
                    IconButton(
                        onClick = onRestartGame,
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart Game", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2C))
        )

        // Pre-Game Staging Instruction Banner
        if (!uiState.isGameStarted) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1C2738),
                border = BorderStroke(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✏️", fontSize = 18.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pre-Race Staging",
                            color = Color(0xFF4FC3F7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Tap any mouse name below to customize, then press START!",
                            color = Color(0xFFCFD8DC),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onStartRace,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("stage_start_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("START", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }

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

                        val canEvaluateBet = uiState.isGameOver

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
                .background(Color(0xFF161622))
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(trackWidthDp)
                    .onGloballyPositioned { parentLayoutCoordinates = it }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Players List Track
                    uiState.players.forEachIndexed { index, player ->
                        PlayerRowTrack(
                            player = player,
                            winningScore = uiState.winningScore,
                            isGameStarted = uiState.isGameStarted,
                            onRenameClick = { renamingPlayer = it },
                            modifier = if (index == 0) {
                                Modifier.onGloballyPositioned { coords ->
                                    parentLayoutCoordinates?.let { parent ->
                                        firstRowTopPx = parent.localPositionOf(coords, Offset.Zero).y
                                    }
                                }
                            } else Modifier
                        )
                    }

                    // Laser / Cat Indicator Row (Only visible when game is started)
                    if (uiState.laserMode != LaserMode.OFF && uiState.isGameStarted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LaserCatRowTrack(
                            uiState = uiState,
                            onCat1Positioned = { coords ->
                                parentLayoutCoordinates?.let { parent ->
                                    val localPos = parent.localPositionOf(coords, Offset(coords.size.width / 2f, 0f))
                                    cat1CenterXPx = localPos.x
                                    cat1TopYPx = localPos.y
                                }
                            },
                            onCat2Positioned = { coords ->
                                parentLayoutCoordinates?.let { parent ->
                                    val localPos = parent.localPositionOf(coords, Offset(coords.size.width / 2f, 0f))
                                    cat2CenterXPx = localPos.x
                                    cat2TopYPx = localPos.y
                                }
                            }
                        )
                    }
                }

                // Laser Beam Flash Overlay covering all mouse rows from down to up
                if (uiState.laserMode != LaserMode.OFF && uiState.isGameStarted) {
                    LaserBeamOverlay(
                        uiState = uiState,
                        firstRowTopPx = firstRowTopPx,
                        cat1CenterXPx = cat1CenterXPx,
                        cat1TopYPx = cat1TopYPx,
                        cat2CenterXPx = cat2CenterXPx,
                        cat2TopYPx = cat2TopYPx,
                        modifier = Modifier.matchParentSize()
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

                if (!uiState.isGameStarted) {
                    Button(
                        onClick = onStartRace,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier.testTag("start_race_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Race", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                } else {
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

    ConfettiCelebrationEffect(
        trigger = if (uiState.countWinners > 0) uiState.countWinners else null,
        modifier = Modifier.fillMaxSize()
    )
}
}

@Composable
private fun PlayerRowTrack(
    player: PlayerUiState,
    winningScore: Int,
    isGameStarted: Boolean,
    onRenameClick: (PlayerUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                if (!isGameStarted) {
                    // Pre-game editable name button
                    Surface(
                        onClick = { onRenameClick(player) },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2B2B3E),
                        border = BorderStroke(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("rename_player_${player.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = player.name.trim(),
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = Color(0xFF81D4FA),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                } else {
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

            // Key on Track (Only visible when game is started and not collected yet)
            if (isGameStarted && player.keyPosition != null && !player.isKeyCollected) {
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

            // Cheese on Track (Only visible when game is started and not collected yet)
            if (isGameStarted && player.cheesePosition != null && !player.isCheeseCollected) {
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

            // Trap on Track (Only visible when game is started and not triggered yet)
            if (isGameStarted && player.trapPosition != null && !player.isTrapTriggered) {
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

            // Confetti celebration when mouse reaches finish line or completes correct prediction streak
            val isAtFinishLine = player.score >= winningScore || player.winnerRank != null
            val isCorrectPrediction = player.winnerRank != null && player.predictedRank != null && player.winnerRank == player.predictedRank

            if (isAtFinishLine) {
                TrackFinishLineConfetti(
                    isFinished = true,
                    isPredictionCorrect = isCorrectPrediction,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun LaserCatRowTrack(
    uiState: GameUiState,
    onCat1Positioned: (LayoutCoordinates) -> Unit = {},
    onCat2Positioned: (LayoutCoordinates) -> Unit = {}
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
                val totalTrackWidth = maxWidth - 36.dp
                val positionFraction = (uiState.laserLineNumber.toFloat() / uiState.winningScore.toFloat()).coerceIn(0f, 1f)
                val targetCatOffset = totalTrackWidth * positionFraction
                val animatedCatOffset by animateDpAsState(
                    targetValue = targetCatOffset,
                    animationSpec = tween(durationMillis = 300),
                    label = "CatOffset"
                )

                Box(
                    modifier = Modifier
                        .offset(x = animatedCatOffset)
                        .onGloballyPositioned { coords -> onCat1Positioned(coords) }
                ) {
                    if (uiState.laserLineTime < uiState.laserLineTimeMax) {
                        // Countdown state
                        val isWarning = uiState.laserLineTime == uiState.laserLineTimeMax - 1
                        val displayText = if (uiState.sneakyCat) "❓" else "${uiState.laserLineTime}"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isWarning) Color(0xFFE53935) else Color(0xFF424242))
                                .border(
                                    if (isWarning) 2.dp else 1.dp,
                                    if (isWarning) Color(0xFFFFD54F) else Color(0xFF757575),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Attack state (Cat with red emitter aura at base of upward laser)
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744).copy(alpha = 0.4f))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_cat),
                                contentDescription = "Laser Cat 1",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFFFF1744), CircleShape)
                            )
                        }
                    }
                }
            }

            // Cat 2 Indicator (Dual Cat)
            if (uiState.dualCat && uiState.laserLineNumber2 > 0) {
                val totalTrackWidth = maxWidth - 36.dp
                val positionFraction2 = (uiState.laserLineNumber2.toFloat() / uiState.winningScore.toFloat()).coerceIn(0f, 1f)
                val targetCatOffset2 = totalTrackWidth * positionFraction2
                val animatedCatOffset2 by animateDpAsState(
                    targetValue = targetCatOffset2,
                    animationSpec = tween(durationMillis = 300),
                    label = "CatOffset2"
                )

                Box(
                    modifier = Modifier
                        .offset(x = animatedCatOffset2)
                        .onGloballyPositioned { coords -> onCat2Positioned(coords) }
                ) {
                    if (uiState.laserLineTime2 < uiState.laserLineTimeMax2) {
                        // Countdown state for Cat 2
                        val isWarning2 = uiState.laserLineTime2 == uiState.laserLineTimeMax2 - 1
                        val displayText2 = if (uiState.sneakyCat) "❓" else "${uiState.laserLineTime2}"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isWarning2) Color(0xFFFF9800) else Color(0xFF2E7D32))
                                .border(
                                    if (isWarning2) 2.dp else 1.dp,
                                    if (isWarning2) Color(0xFFFFD54F) else Color(0xFF81C784),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayText2,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Attack state for Cat 2 (Cyan/Emerald aura at base of upward laser)
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676).copy(alpha = 0.4f))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_cat),
                                contentDescription = "Laser Cat 2",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFF00E676), CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaserBeamOverlay(
    uiState: GameUiState,
    firstRowTopPx: Float,
    cat1CenterXPx: Float,
    cat1TopYPx: Float,
    cat2CenterXPx: Float,
    cat2TopYPx: Float,
    modifier: Modifier = Modifier
) {
    val isCat1Striking = uiState.laserLineNumber > 0 && uiState.laserLineTime >= uiState.laserLineTimeMax
    val isCat1Warning = uiState.laserLineNumber > 0 && uiState.laserLineTime == uiState.laserLineTimeMax - 1

    val isCat2Striking = uiState.dualCat && uiState.laserLineNumber2 > 0 && uiState.laserLineTime2 >= uiState.laserLineTimeMax2
    val isCat2Warning = uiState.dualCat && uiState.laserLineNumber2 > 0 && uiState.laserLineTime2 == uiState.laserLineTimeMax2 - 1

    // Upward flash animation for Cat 1 (from cat at bottom up to first mouse row)
    val shootProgress1 = remember { Animatable(0f) }
    LaunchedEffect(uiState.laserLineNumber, isCat1Striking) {
        if (isCat1Striking) {
            shootProgress1.snapTo(0f)
            shootProgress1.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
            )
        } else {
            shootProgress1.snapTo(0f)
        }
    }

    // Upward flash animation for Cat 2
    val shootProgress2 = remember { Animatable(0f) }
    LaunchedEffect(uiState.laserLineNumber2, isCat2Striking) {
        if (isCat2Striking) {
            shootProgress2.snapTo(0f)
            shootProgress2.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
            )
        } else {
            shootProgress2.snapTo(0f)
        }
    }

    // Continuous energetic flicker & width pulse during laser fire
    val infiniteTransition = rememberInfiniteTransition(label = "laser_flicker")
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flickerAlpha"
    )
    val pulseWidth by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseWidth"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val laneStartX = 142.dp.toPx()
        val totalLaneWidth = (canvasWidth - 154.dp.toPx()).coerceAtLeast(100f)
        val targetTopY = if (firstRowTopPx > 0f) (firstRowTopPx - 8.dp.toPx()).coerceAtLeast(0f) else 4.dp.toPx()

        // --- CAT 1 LASER STRIKE (RED / NEON CRIMSON) ---
        if (uiState.laserLineNumber > 0) {
            val progress1 = (uiState.laserLineNumber.toFloat() / uiState.winningScore.toFloat()).coerceIn(0f, 1f)
            val fallbackCat1X = laneStartX + (totalLaneWidth - 36.dp.toPx()) * progress1 + 18.dp.toPx()
            val cat1X = if (cat1CenterXPx > 0f) cat1CenterXPx else fallbackCat1X
            val cat1Y = if (cat1TopYPx > 0f) cat1TopYPx else (canvasHeight - 24.dp.toPx())

            if (isCat1Striking && shootProgress1.value > 0f) {
                // The laser flashes from down (cat1Y) to up (targetTopY above first mouse row)
                val beamHeadY = cat1Y - (cat1Y - targetTopY) * shootProgress1.value

                // 1. Ambient strike glow column across all mouse rows
                val glowHalfWidth = 24.dp.toPx() * pulseWidth
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33FF1744),
                            Color(0x88FF1744),
                            Color(0x33FF1744),
                            Color.Transparent
                        ),
                        startX = cat1X - glowHalfWidth,
                        endX = cat1X + glowHalfWidth
                    ),
                    topLeft = Offset(cat1X - glowHalfWidth, beamHeadY),
                    size = androidx.compose.ui.geometry.Size(glowHalfWidth * 2f, (cat1Y - beamHeadY).coerceAtLeast(1f))
                )

                // 2. Outer energetic laser beam
                drawLine(
                    color = Color(0xFFFF1744).copy(alpha = 0.85f * flickerAlpha),
                    start = Offset(cat1X, cat1Y),
                    end = Offset(cat1X, beamHeadY),
                    strokeWidth = 10.dp.toPx() * pulseWidth,
                    cap = StrokeCap.Round
                )

                // 3. Inner neon laser core
                drawLine(
                    color = Color(0xFFFF5252).copy(alpha = flickerAlpha),
                    start = Offset(cat1X, cat1Y),
                    end = Offset(cat1X, beamHeadY),
                    strokeWidth = 5.dp.toPx() * pulseWidth,
                    cap = StrokeCap.Round
                )

                // 4. Brilliant white/yellow core line
                drawLine(
                    color = Color(0xFFFFFDE7).copy(alpha = flickerAlpha),
                    start = Offset(cat1X, cat1Y),
                    end = Offset(cat1X, beamHeadY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 5. Upward traveling Laser Head Flare (The flashing tip)
                val headRadius = 13.dp.toPx() * pulseWidth
                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = 0.9f * flickerAlpha),
                    radius = headRadius,
                    center = Offset(cat1X, beamHeadY)
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = flickerAlpha),
                    radius = headRadius * 0.55f,
                    center = Offset(cat1X, beamHeadY)
                )
                drawCircle(
                    color = Color.White,
                    radius = headRadius * 0.28f,
                    center = Offset(cat1X, beamHeadY)
                )

                // Cross sparkle rays at the beam head
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * flickerAlpha),
                    start = Offset(cat1X - 16.dp.toPx(), beamHeadY),
                    end = Offset(cat1X + 16.dp.toPx(), beamHeadY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * flickerAlpha),
                    start = Offset(cat1X, beamHeadY - 14.dp.toPx()),
                    end = Offset(cat1X, beamHeadY + 14.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 6. Emitter corona burst at the cat (bottom origin)
                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = 0.8f * flickerAlpha),
                    radius = 10.dp.toPx() * pulseWidth,
                    center = Offset(cat1X, cat1Y)
                )
                drawCircle(
                    color = Color(0xFFFFFDE7),
                    radius = 4.dp.toPx(),
                    center = Offset(cat1X, cat1Y)
                )
            } else if (isCat1Warning) {
                // Subtle warning targeting guide line from cat up through all mouse rows
                drawLine(
                    color = Color(0xFFFF5252).copy(alpha = 0.4f * flickerAlpha),
                    start = Offset(cat1X, cat1Y),
                    end = Offset(cat1X, targetTopY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // --- CAT 2 LASER STRIKE (DUAL CAT: CYAN / EMERALD) ---
        if (uiState.dualCat && uiState.laserLineNumber2 > 0) {
            val progress2 = (uiState.laserLineNumber2.toFloat() / uiState.winningScore.toFloat()).coerceIn(0f, 1f)
            val fallbackCat2X = laneStartX + (totalLaneWidth - 36.dp.toPx()) * progress2 + 18.dp.toPx()
            val cat2X = if (cat2CenterXPx > 0f) cat2CenterXPx else fallbackCat2X
            val cat2Y = if (cat2TopYPx > 0f) cat2TopYPx else (canvasHeight - 24.dp.toPx())

            if (isCat2Striking && shootProgress2.value > 0f) {
                val beamHeadY2 = cat2Y - (cat2Y - targetTopY) * shootProgress2.value

                // 1. Ambient strike glow column
                val glowHalfWidth2 = 24.dp.toPx() * pulseWidth
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x3300E5FF),
                            Color(0x8800E5FF),
                            Color(0x3300E5FF),
                            Color.Transparent
                        ),
                        startX = cat2X - glowHalfWidth2,
                        endX = cat2X + glowHalfWidth2
                    ),
                    topLeft = Offset(cat2X - glowHalfWidth2, beamHeadY2),
                    size = androidx.compose.ui.geometry.Size(glowHalfWidth2 * 2f, (cat2Y - beamHeadY2).coerceAtLeast(1f))
                )

                // 2. Outer energetic laser beam
                drawLine(
                    color = Color(0xFF00E676).copy(alpha = 0.85f * flickerAlpha),
                    start = Offset(cat2X, cat2Y),
                    end = Offset(cat2X, beamHeadY2),
                    strokeWidth = 10.dp.toPx() * pulseWidth,
                    cap = StrokeCap.Round
                )

                // 3. Inner neon laser core
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = flickerAlpha),
                    start = Offset(cat2X, cat2Y),
                    end = Offset(cat2X, beamHeadY2),
                    strokeWidth = 5.dp.toPx() * pulseWidth,
                    cap = StrokeCap.Round
                )

                // 4. White core
                drawLine(
                    color = Color.White.copy(alpha = flickerAlpha),
                    start = Offset(cat2X, cat2Y),
                    end = Offset(cat2X, beamHeadY2),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 5. Upward traveling Laser Head Flare
                val headRadius2 = 13.dp.toPx() * pulseWidth
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.9f * flickerAlpha),
                    radius = headRadius2,
                    center = Offset(cat2X, beamHeadY2)
                )
                drawCircle(
                    color = Color(0xFFB9F6CA).copy(alpha = flickerAlpha),
                    radius = headRadius2 * 0.55f,
                    center = Offset(cat2X, beamHeadY2)
                )
                drawCircle(
                    color = Color.White,
                    radius = headRadius2 * 0.28f,
                    center = Offset(cat2X, beamHeadY2)
                )

                // Cross sparkle rays at the beam head
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * flickerAlpha),
                    start = Offset(cat2X - 16.dp.toPx(), beamHeadY2),
                    end = Offset(cat2X + 16.dp.toPx(), beamHeadY2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * flickerAlpha),
                    start = Offset(cat2X, beamHeadY2 - 14.dp.toPx()),
                    end = Offset(cat2X, beamHeadY2 + 14.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 6. Emitter corona burst at Cat 2
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.8f * flickerAlpha),
                    radius = 10.dp.toPx() * pulseWidth,
                    center = Offset(cat2X, cat2Y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(cat2X, cat2Y)
                )
            } else if (isCat2Warning) {
                // Subtle warning sight line
                drawLine(
                    color = Color(0xFF00E676).copy(alpha = 0.4f * flickerAlpha),
                    start = Offset(cat2X, cat2Y),
                    end = Offset(cat2X, targetTopY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RenamePlayerDialog(
    player: PlayerUiState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameText by remember { mutableStateOf(player.name.trim()) }
    val presets = listOf("Speedy", "Jerry", "Mighty", "Bolt", "Pip", "Flash", "Turbo", "Squeak", "Nugget", "Cheesy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🐭", fontSize = 22.sp)
                Text(
                    text = "Rename Mouse #${player.id}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { if (it.length <= 16) nameText = it },
                    label = { Text("Mouse Name") },
                    placeholder = { Text("e.g. Speedy, Jerry, Alice") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFF4A4A62),
                        focusedLabelColor = Color(0xFFFFD700),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mouse_name_input")
                )

                Text(
                    text = "Quick Presets:",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Preset Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        Surface(
                            onClick = { nameText = preset },
                            shape = RoundedCornerShape(8.dp),
                            color = if (nameText.equals(preset, ignoreCase = true)) Color(0xFFFFD700) else Color(0xFF2A2A3C),
                            border = BorderStroke(1.dp, if (nameText.equals(preset, ignoreCase = true)) Color(0xFFFFD700) else Color(0xFF424258))
                        ) {
                            Text(
                                text = preset,
                                color = if (nameText.equals(preset, ignoreCase = true)) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(nameText.trim().ifEmpty { "Player ${player.id}" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                modifier = Modifier.testTag("confirm_rename_button")
            ) {
                Text("Save Name", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF1E1E2C),
        shape = RoundedCornerShape(16.dp)
    )
}

