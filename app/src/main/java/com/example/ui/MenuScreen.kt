package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameSettings
import com.example.model.LaserMode
import com.example.model.WinMode

@Composable
fun MenuScreen(
    settings: GameSettings,
    matchHistory: List<com.example.model.MatchRecord> = emptyList(),
    onClearHistory: () -> Unit = {},
    onPlayerCountChange: (Int) -> Unit,
    onLaserModeToggle: () -> Unit,
    onSelectLaserMode: (LaserMode) -> Unit = {},
    onWinningScoreChange: (Int) -> Unit,
    onFastCatToggle: () -> Unit,
    onCheeseToggle: () -> Unit = {},
    onTrapToggle: () -> Unit = {},
    onSneakyCatToggle: () -> Unit = {},
    onDualCatToggle: () -> Unit = {},
    onBetAmountChange: (Int) -> Unit = {},
    onBetTypeChange: (com.example.model.BetType) -> Unit = {},
    onToggleGuess: (Int) -> Unit = {},
    onStartGame: (WinMode) -> Unit
) {
    var showHistoryDialog by remember { mutableStateOf(false) }

    if (showHistoryDialog) {
        Dialog(onDismissRequest = { showHistoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1E28),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📊", fontSize = 20.sp)
                            Text(
                                text = "Score & Match History",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (matchHistory.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearHistory,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Clear History",
                                        tint = Color(0xFFFF7043),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showHistoryDialog = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF353545))

                    if (matchHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No match history recorded yet.\nPredict finish order and complete a game!",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matchHistory.forEach { record ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF262636),
                                    border = BorderStroke(1.dp, Color(0xFF3A3A4C)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Header Row: Timestamp + Earned Points
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📅 ${record.timestamp}",
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            val isWin = record.pointsEarned >= 0
                                            val pointsStr = if (isWin) "+${record.pointsEarned}" else "${record.pointsEarned}"
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isWin) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF7043).copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, if (isWin) Color(0xFF00E676) else Color(0xFFFF7043))
                                            ) {
                                                Text(
                                                    text = "$pointsStr Pts",
                                                    color = if (isWin) Color(0xFF00E676) else Color(0xFFFF7043),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        // Mode Details (Cheese, Trap, Laser Type, Bet Odds)
                                        Text(
                                            text = "⚙️ ${record.modeSummary}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal
                                        )

                                        // Prediction vs Actual Winners
                                        Text(
                                            text = "🎯 Predicted: ${record.predictionsSummary}  →  🏁 Result: ${record.winnersSummary}",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showHistoryDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("close_history_dialog"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF353545))
                    ) {
                        Text("Close History", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🐭", fontSize = 24.sp)
                    Text(
                        text = "Mouse Game Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(text = "🐱", fontSize = 24.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF2E2E3E))

                // Scrollable Settings Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                // Core Match Settings (Numeric Steppers in individual rows)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingItem(
                        label = "Players",
                        valueText = "${settings.playerCount}",
                        icon = Icons.Default.Group,
                        onDecrement = { onPlayerCountChange(-1) },
                        onIncrement = { onPlayerCountChange(1) }
                    )

                    SettingItem(
                        label = "Winning Score",
                        valueText = "${settings.winningScore}",
                        icon = Icons.Default.EmojiEvents,
                        onDecrement = { onWinningScoreChange(-1) },
                        onIncrement = { onWinningScoreChange(1) }
                    )
                }

                // Laser Cat Attack Mode Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚡ Laser Cat Attack Mode",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LaserMode.values().forEach { mode ->
                            val isSelected = settings.laserMode == mode
                            val modeColor = when (mode) {
                                LaserMode.OFF -> Color(0xFF888888)
                                LaserMode.REFRESH -> Color(0xFFFFB74D)
                                LaserMode.KILL -> Color(0xFFFF5252)
                                LaserMode.KEY_CAGE -> Color(0xFFAB47BC)
                            }

                            Surface(
                                onClick = { onSelectLaserMode(mode) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) modeColor.copy(alpha = 0.25f) else Color(0xFF22222E),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) modeColor else Color(0xFF353545)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("laser_mode_${mode.name}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.displayName,
                                        color = if (isSelected) Color.White else Color(0xFF888898),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Cat Options (ALL 3 IN 1 LINE!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🐱 Cat Modifiers",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OptionButton(
                            title = "Fast Cat",
                            subtitle = "2x Speed",
                            emoji = "⚡",
                            checked = settings.fastCat,
                            activeColor = Color(0xFF66BB6A),
                            onClick = onFastCatToggle,
                            modifier = Modifier.weight(1f),
                            testTag = "toggle_fast_cat"
                        )

                        OptionButton(
                            title = "Sneaky",
                            subtitle = "Hide ❓",
                            emoji = "😼",
                            checked = settings.sneakyCat,
                            activeColor = Color(0xFF26A69A),
                            onClick = onSneakyCatToggle,
                            modifier = Modifier.weight(1f),
                            testTag = "toggle_sneaky_cat"
                        )

                        OptionButton(
                            title = "Dual Cat",
                            subtitle = "2 Cats",
                            emoji = "⚡",
                            checked = settings.dualCat,
                            activeColor = Color(0xFF00E676),
                            onClick = onDualCatToggle,
                            modifier = Modifier.weight(1f),
                            testTag = "toggle_dual_cat"
                        )
                    }
                }

                // Mouse Items & Traps
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🐭 Mouse Items & Traps",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OptionButton(
                            title = "Cheese",
                            subtitle = "+2 Boost",
                            emoji = "🧀",
                            checked = settings.hasCheese,
                            activeColor = Color(0xFFFFD54F),
                            onClick = onCheeseToggle,
                            modifier = Modifier.weight(1f),
                            testTag = "toggle_cheese"
                        )

                        OptionButton(
                            title = "Trap",
                            subtitle = "Stun",
                            emoji = "🪤",
                            checked = settings.hasTrap,
                            activeColor = Color(0xFFFF7043),
                            onClick = onTrapToggle,
                            modifier = Modifier.weight(1f),
                            testTag = "toggle_trap"
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF2E2E3E))

                // 🎲 Betting Points & Wager Odds System
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🎲", fontSize = 16.sp)
                            Text(
                                text = "Betting System (Active on Prediction)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Betting Wager Stake Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Bet Stake:",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.width(52.dp)
                        )
                        listOf(10, 25, 50, 100).forEach { amount ->
                            val isSelected = settings.betAmount == amount
                            Surface(
                                onClick = { onBetAmountChange(amount) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFFFB74D).copy(alpha = 0.25f) else Color(0xFF22222E),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFB74D) else Color(0xFF353545)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$amount Pts",
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Bet Type Rule
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Bet Odds:",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.width(52.dp)
                        )
                        com.example.model.BetType.values().forEach { betType ->
                            val isSelected = settings.betType == betType
                            Surface(
                                onClick = { onBetTypeChange(betType) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF81D4FA).copy(alpha = 0.25f) else Color(0xFF22222E),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF81D4FA) else Color(0xFF353545)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = betType.displayName,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF2E2E3E))

                // 📊 Score & Match History Button Card
                Surface(
                    onClick = { showHistoryDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF262636),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_history_button")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "📊", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Score & Match History",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (matchHistory.isEmpty()) "No matches played yet" else "${matchHistory.size} match records saved",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text(
                                text = "VIEW 📊",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF2E2E3E))

                // Predict Finish Order Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFFFFB74D),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Predict Finish Order (Optional)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (playerId in 1..settings.playerCount) {
                            val rankIndex = settings.guesses.indexOf(playerId)
                            val isGuessed = rankIndex >= 0
                            val rankNumber = rankIndex + 1

                            val backgroundColor = if (isGuessed) {
                                when (rankNumber) {
                                    1 -> Color(0xFFFFD700)
                                    2 -> Color(0xFFE0E0E0)
                                    3 -> Color(0xFFFFB74D)
                                    else -> Color(0xFF81D4FA)
                                }
                            } else {
                                Color(0xFF22222E)
                            }

                            val textColor = if (isGuessed) Color.Black else Color.White

                            Surface(
                                onClick = { onToggleGuess(playerId) },
                                shape = RoundedCornerShape(10.dp),
                                color = backgroundColor,
                                border = if (isGuessed) null else BorderStroke(1.dp, Color(0xFF353545)),
                                modifier = Modifier.testTag("guess_player_$playerId")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "P$playerId",
                                        color = textColor,
                                        fontWeight = if (isGuessed) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                    if (isGuessed) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "#$rankNumber",
                                                color = textColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF2E2E3E))

                // Start Game Buttons (Fixed at Bottom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStartGame(WinMode.WIN_1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("start_win_1_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WIN 1",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onStartGame(WinMode.WIN_ALL) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("start_win_all_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WIN ALL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    title: String,
    subtitle: String,
    emoji: String,
    checked: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val backgroundColor = if (checked) activeColor.copy(alpha = 0.22f) else Color(0xFF22222D)
    val borderColor = if (checked) activeColor else Color(0xFF353545)
    val titleColor = if (checked) Color.White else Color(0xFF888898)
    val subtitleColor = if (checked) activeColor else Color(0xFF656575)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(width = if (checked) 1.5.dp else 1.dp, color = borderColor),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = emoji, fontSize = 14.sp)
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Text(
                text = subtitle,
                color = subtitleColor,
                fontSize = 10.sp,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingItem(
    label: String,
    valueText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF22222E))
            .border(BorderStroke(1.dp, Color(0xFF353545)), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(20.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF353548), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Text(
                text = valueText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.widthIn(min = 28.dp)
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF353548), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
