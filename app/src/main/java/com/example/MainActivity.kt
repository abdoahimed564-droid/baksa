package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CustomQuestionsScreen
import com.example.ui.screens.GameConfigScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.PlayerSetupScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.theme.BekasaTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BekasaTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    BekasaApp()
                }
            }
        }
    }
}

@Composable
fun BekasaApp(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (uiState.currentScreen) {
            Screen.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel)
            Screen.PLAYERS -> PlayerSetupScreen(uiState = uiState, viewModel = viewModel)
            Screen.GAME_CONFIG -> GameConfigScreen(uiState = uiState, viewModel = viewModel)
            Screen.PLAY -> PlayScreen(uiState = uiState, viewModel = viewModel)
            Screen.GAME_OVER -> GameOverScreen(uiState = uiState, viewModel = viewModel)
            Screen.CUSTOM_QUESTIONS -> CustomQuestionsScreen(uiState = uiState, viewModel = viewModel)
            Screen.RULES -> RulesScreen(uiState = uiState, viewModel = viewModel)
        }
    }
}
