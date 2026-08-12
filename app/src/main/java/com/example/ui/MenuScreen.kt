package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.GameSettings
import com.example.model.LaserMode
import com.example.model.WinMode

@Composable
fun MenuScreen(
    settings: GameSettings,
    onPlayerCountChange: (Int) -> Unit,
    onLaserModeToggle: () -> Unit,
    onWinningScoreChange: (Int) -> Unit,
    onFastCatToggle: () -> Unit,
    onStartGame: (WinMode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E28)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🐭",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "Mouse Game",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "🐱",
                        fontSize = 32.sp
                    )
                }

                Text(
                    text = "Select game settings and race to victory!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                HorizontalDivider(color = Color(0xFF2E2E3E))

                // Settings Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Players Setting
                    SettingItem(
                        label = "Players",
                        valueText = "${settings.playerCount}",
                        icon = Icons.Default.Group,
                        onDecrement = { onPlayerCountChange(-1) },
                        onIncrement = { onPlayerCountChange(1) }
                    )

                    // Laser Setting
                    SettingItemToggle(
                        label = "Laser Line",
                        valueText = settings.laserMode.displayName,
                        icon = Icons.Default.Bolt,
                        color = when (settings.laserMode) {
                            LaserMode.OFF -> Color.Gray
                            LaserMode.REFRESH -> Color(0xFFFFB74D)
                            LaserMode.KILL -> Color(0xFFFF5252)
                        },
                        onClick = onLaserModeToggle
                    )

                    // Target Score Setting
                    SettingItem(
                        label = "Winning Score",
                        valueText = "${settings.winningScore}",
                        icon = Icons.Default.EmojiEvents,
                        onDecrement = { onWinningScoreChange(-1) },
                        onIncrement = { onWinningScoreChange(1) }
                    )

                    // Fast Cat Setting
                    SettingItemToggle(
                        label = "Fast Cat",
                        valueText = if (settings.fastCat) "True" else "False",
                        icon = Icons.Default.Speed,
                        color = if (settings.fastCat) Color(0xFF66BB6A) else Color.Gray,
                        onClick = onFastCatToggle
                    )
                }

                HorizontalDivider(color = Color(0xFF2E2E3E))

                // Mode Selection Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onStartGame(WinMode.WIN_1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_win_1_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. WIN 1 (First to Finish)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onStartGame(WinMode.WIN_ALL) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_win_all_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. WIN ALL (Race All Players)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
            .background(Color(0xFF282836))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFFB74D))
            Text(label, color = Color.White, fontWeight = FontWeight.Medium)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF38384A), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Text(
                text = valueText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 28.dp),
                fontSize = 16.sp
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF38384A), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SettingItemToggle(
    label: String,
    valueText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF282836))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(label, color = Color.White, fontWeight = FontWeight.Medium)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = valueText,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
