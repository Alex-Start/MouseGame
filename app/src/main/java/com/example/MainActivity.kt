package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GameScreen
import com.example.ui.GameState
import com.example.ui.GameViewModel
import com.example.ui.MenuScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MouseGameApp()
                }
            }
        }
    }
}

@Composable
fun MouseGameApp(viewModel: GameViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    when (val state = gameState) {
        is GameState.Menu -> {
            MenuScreen(
                settings = settings,
                onPlayerCountChange = viewModel::updatePlayerCount,
                onLaserModeToggle = viewModel::toggleLaserMode,
                onSelectLaserMode = viewModel::setLaserMode,
                onWinningScoreChange = viewModel::updateWinningScore,
                onFastCatToggle = viewModel::toggleFastCat,
                onCheeseToggle = viewModel::toggleCheese,
                onTrapToggle = viewModel::toggleTrap,
                onSneakyCatToggle = viewModel::toggleSneakyCat,
                onDualCatToggle = viewModel::toggleDualCat,
                onToggleGuess = viewModel::toggleGuess,
                onStartGame = viewModel::startGame
            )
        }
        is GameState.Playing -> {
            GameScreen(
                uiState = state.uiState,
                onReturnToMenu = viewModel::returnToMenu,
                onRestartGame = viewModel::restartGame,
                onToggleMute = viewModel::toggleMute
            )
        }
    }
}

