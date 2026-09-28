package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameMode
import com.example.data.model.QuestionCategory
import com.example.ui.components.BekasaTopBar
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameConfigScreen(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            BekasaTopBar(
                title = "إعدادات الجلسة ⚙️",
                onBackClick = { viewModel.navigateBack() },
                soundEnabled = uiState.settings.soundEnabled,
                onSoundToggle = { viewModel.toggleSound() }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.startNewGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .testTag("save_and_start_button")
                ) {
                    Text(
                        text = "بدء الجلسة بهذه الإعدادات 🎲",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mode Selector
            Text(
                text = "اختر وضع اللعبة:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            GameMode.values().forEach { mode ->
                val isSelected = uiState.settings.mode == mode
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            viewModel.updateSettings(uiState.settings.copy(mode = mode))
                        }
                        .testTag("mode_${mode.name}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = mode.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.titleAr,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mode.subtitleAr,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Timer Selection
            Text(
                text = "مؤقت الهبد والإقناع لكل سؤال:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    30 to "30 ث",
                    45 to "45 ث",
                    60 to "60 ث",
                    90 to "90 ث",
                    0 to "بدون وقت"
                ).forEach { (seconds, label) ->
                    val isSelected = uiState.settings.roundTimerSeconds == seconds
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.updateSettings(uiState.settings.copy(roundTimerSeconds = seconds))
                        },
                        label = { Text(label) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("timer_chip_$seconds")
                    )
                }
            }

            // Rounds Selection
            Text(
                text = "عدد الجولات:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    5 to "5 جولات",
                    10 to "10 جولات",
                    15 to "15 جولة",
                    0 to "مفتوح ♾️"
                ).forEach { (rounds, label) ->
                    val isSelected = uiState.settings.totalRounds == rounds
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.updateSettings(uiState.settings.copy(totalRounds = rounds))
                        },
                        label = { Text(label) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rounds_chip_$rounds")
                    )
                }
            }

            // Categories Selection
            Text(
                text = "تصنيفات الأسئلة المفضلة:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuestionCategory.values().forEach { category ->
                    val isSelected = uiState.settings.selectedCategories.contains(category)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleCategory(category) },
                        label = {
                            Text("${category.icon} ${category.titleAr}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalPurple.copy(alpha = 0.25f),
                            selectedLabelColor = RoyalPurple
                        ),
                        modifier = Modifier.testTag("category_chip_${category.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
