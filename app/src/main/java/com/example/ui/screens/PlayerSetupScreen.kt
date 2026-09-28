package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BekasaTopBar
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.BluffRed
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerSetupScreen(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    var newPlayerName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("😎") }
    var selectedColor by remember { mutableLongStateOf(0xFF7C3AED) }

    val emojiOptions = listOf("😎", "🎭", "👑", "🔥", "🦁", "⚡", "🧠", "✨", "🕵️", "🤠", "🍕", "🚀")
    val colorOptions = listOf(
        0xFF7C3AED, // Purple
        0xFFEC4899, // Pink
        0xFFF59E0B, // Amber Gold
        0xFF10B981, // Mint
        0xFF3B82F6, // Blue
        0xFFEF4444, // Red
        0xFF8B5CF6  // Violet
    )

    fun submitNewPlayer() {
        if (newPlayerName.isNotBlank()) {
            viewModel.addPlayer(newPlayerName, selectedEmoji, selectedColor)
            newPlayerName = ""
            // Cycle emoji & color automatically for fun!
            val nextEmoji = emojiOptions.random()
            val nextColor = colorOptions.random()
            selectedEmoji = nextEmoji
            selectedColor = nextColor
        }
    }

    Scaffold(
        topBar = {
            BekasaTopBar(
                title = "إضافة أسماء اللاعبين 👥",
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
                    onClick = {
                        if (uiState.players.size >= 2) {
                            viewModel.startNewGame()
                        }
                    },
                    enabled = uiState.players.size >= 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrophyGold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                        .testTag("confirm_and_play_button")
                ) {
                    Text(
                        text = if (uiState.players.size >= 2) "بدء اللعبة (${uiState.players.size} لاعبين) 🚀" else "أضف لاعبين اثنين على الأقل للبدء",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.players.size >= 2) Color.Black else Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Fast Manual Name Input Box
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "إضافة لاعب",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "اكتب اسم اللاعب يدوياً:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newPlayerName,
                                onValueChange = { newPlayerName = it },
                                placeholder = { Text("اكتب الاسم (مثلاً: محمد، يوسف، هبة)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { submitNewPlayer() }),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("player_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { submitNewPlayer() },
                                enabled = newPlayerName.isNotBlank(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("add_player_submit_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "إضافة", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Emoji & Color Quick customizer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رمز اللاعب واللون:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                emojiOptions.take(6).forEach { emoji ->
                                    val isSelected = emoji == selectedEmoji
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                            .border(
                                                if (isSelected) 2.dp else 0.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                CircleShape
                                            )
                                            .clickable { selectedEmoji = emoji }
                                    ) {
                                        Text(text = emoji, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Clear or Presets Helpers
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اللاعبون المضافون (${uiState.players.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    if (uiState.players.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearAllPlayers() },
                            modifier = Modifier.testTag("clear_all_players_button")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = BluffRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "مسح الكل", color = BluffRed, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Empty state helper
            if (uiState.players.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "✍️", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "القائمة فارغة، ابدأ بإضافة أسامي أصحابك يدوياً من المربع بالأعلى!",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { viewModel.loadPreset("friends") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("أو حمّل شلة أصحاب جاهزة 🔥")
                            }
                        }
                    }
                }
            } else {
                // Players List
                items(uiState.players) { player ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_item_${player.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlayerAvatar(player = player, size = 42.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "النقاط الحالية: ${player.score}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.removePlayer(player.id) },
                                modifier = Modifier.testTag("delete_player_${player.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف اللاعب",
                                    tint = BluffRed.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
