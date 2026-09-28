package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BekasaTopBar
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.BluffRed
import com.example.ui.theme.PartyPink
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.RoyalPurpleLight
import com.example.ui.theme.TrophyGold
import com.example.ui.theme.TruthGreen
import com.example.ui.viewmodel.AnonymousBluffOption
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.RoundPhase

@Composable
fun PlayScreen(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    val question = uiState.currentQuestion

    Scaffold(
        topBar = {
            BekasaTopBar(
                title = if (uiState.settings.totalRounds > 0) "جولة ${uiState.currentRound} من ${uiState.settings.totalRounds}" else "الجولة ${uiState.currentRound} 🎭",
                onBackClick = { showExitDialog = true },
                soundEnabled = uiState.settings.soundEnabled,
                onSoundToggle = { viewModel.toggleSound() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.roundPhase) {
                RoundPhase.WRITING_HANDOFF -> {
                    WritingHandoffView(uiState = uiState, viewModel = viewModel)
                }
                RoundPhase.WRITING_INPUT -> {
                    WritingInputView(uiState = uiState, viewModel = viewModel)
                }
                RoundPhase.VOTING_HANDOFF -> {
                    VotingHandoffView(uiState = uiState, viewModel = viewModel)
                }
                RoundPhase.VOTING_INPUT -> {
                    VotingInputView(uiState = uiState, viewModel = viewModel)
                }
                RoundPhase.ROUND_REVEAL -> {
                    RoundRevealView(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "إنهاء الجلسة؟ 🏁",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("هل تريد إنهاء اللعبة الحالية ورؤية الترتيب النهائي وتتويج ملك البكاسة؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.endGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrophyGold)
                ) {
                    Text("إعلان النتائج 👑", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("متابعة اللعب")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 1. Hand-off view before writing bluff
// ---------------------------------------------------------------------------
@Composable
private fun WritingHandoffView(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    val currentWriter = uiState.currentWriter ?: return
    val totalWriters = uiState.writingQueue.size
    val currentNum = uiState.currentWriterIndex + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = RoyalPurple.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "مرحلة تأليف الهبدات • اللاعب $currentNum من $totalWriters",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = RoyalPurpleLight,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = TrophyGold,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "📱 سلّم الهاتف في سرية إلى:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlayerAvatar(player = currentWriter, size = 80.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentWriter.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TrophyGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "سؤال الجولة مقفول وثابت للجميع! اقرأ السؤال واكتب هبدتك المقنعة عشان تخدع الكل في التصويت!",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startWritingForCurrentPlayer() },
            colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("ready_to_write_button")
        ) {
            Text(
                text = "أنا ${currentWriter.name}، وجاهز للهبد! ✍️",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Secret Bluff Writing View
// ---------------------------------------------------------------------------
@Composable
private fun WritingInputView(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    val currentWriter = uiState.currentWriter ?: return
    val question = uiState.currentQuestion
    val scrollState = rememberScrollState()

    var bluffText by remember(currentWriter.id) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(player = currentWriter, size = 38.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "دور: ${currentWriter.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "اكتب هبدتك في سرية تامة 🤫",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = RoyalPurple.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "سؤال #${question?.id ?: 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RoyalPurpleLight,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fixed Locked Question Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "السؤال مقفول",
                        tint = TrophyGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "السؤال المقفول (نفس السؤال لكل اللاعبين):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TrophyGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = question?.text ?: "سؤال الجولة...",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.testTag("locked_question_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Bluff Hint Helper
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleBluffHint() }
                .testTag("hint_toggle_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiObjects,
                            contentDescription = null,
                            tint = TrophyGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 فكرة هبد للمساعدة (لو محتار ومفلس)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Icon(
                        imageVector = if (uiState.showBluffHint) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showBluffHint,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = question?.bluffHint ?: "اهبد بأي تفاصيل تاريخية أو أسماء علماء من خيالك!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bluff text field
        OutlinedTextField(
            value = bluffText,
            onValueChange = { bluffText = it },
            placeholder = { Text("اكتب هبدتك المقنعة هنا.. خليها تبان علمية أو واقعية عشان الكل يصدقها!") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("bluff_input_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.submitPlayerBluff(bluffText)
                bluffText = ""
            },
            enabled = bluffText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_bluff_button")
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ هبدتي وتمرير الهاتف 🔒",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Hand-off view before voting
// ---------------------------------------------------------------------------
@Composable
private fun VotingHandoffView(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    val currentVoter = uiState.currentVoter ?: return
    val totalVoters = uiState.votingQueue.size
    val currentNum = uiState.currentVoterIndex + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = PartyPink.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "مرحلة كشف الهبدات والتصويت • المصوت $currentNum من $totalVoters",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = PartyPink,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.HowToVote,
            contentDescription = null,
            tint = TrophyGold,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🗳️ سلّم الهاتف للتصويت إلى:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        PlayerAvatar(player = currentVoter, size = 80.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentVoter.name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = TrophyGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "🔒 قانون البكاسة: إجابتك الخاصة محجوبة عنك تماماً! صوت فقط لأكثر هبدة أقنعتك من بين أصحابك!",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startVotingForCurrentPlayer() },
            colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("ready_to_vote_button")
        ) {
            Text(
                text = "أنا ${currentVoter.name}، وجاهز للتصويت! 🗳️",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. Voting Input View (Excluding Voter's Own Answer)
// ---------------------------------------------------------------------------
@Composable
private fun VotingInputView(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    val currentVoter = uiState.currentVoter ?: return
    val question = uiState.currentQuestion
    val options = uiState.currentVoterOptions
    val scrollState = rememberScrollState()

    var selectedOptionOwnerId by remember {
        mutableStateOf(options.firstOrNull()?.ownerPlayerId ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(player = currentVoter, size = 38.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "المصوت: ${currentVoter.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "اختر الإجابة الأكثر إقناعاً 🎯",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = PartyPink.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🔒 إجابتك مستثناة",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PartyPink,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question reminder
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "السؤال كان:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question?.text ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "أي هبدة من الخيارات دي هي الأقنع والأفضل؟",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Anonymous Options List
        options.forEachIndexed { index, option ->
            val isSelected = option.ownerPlayerId == selectedOptionOwnerId
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .border(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { selectedOptionOwnerId = option.ownerPlayerId }
                    .testTag("voting_option_$index")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "خيار #${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تم الاختيار",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedOptionOwnerId.isNotBlank()) {
                    viewModel.submitVote(selectedOptionOwnerId)
                }
            },
            enabled = selectedOptionOwnerId.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("confirm_vote_button")
        ) {
            Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تأكيد تصويتي لهذا الخيار 🗳️",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 5. Round Reveal View (Winner, Bluff Owners & Shocking Real Truth)
// ---------------------------------------------------------------------------
@Composable
private fun RoundRevealView(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    val winningPlayer = uiState.winningPlayer
    val winningBluffText = uiState.winningBluffText
    val highestVotes = uiState.highestVotesCount
    val question = uiState.currentQuestion
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Winning Card Celebration
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, TrophyGold, RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                TrophyGold.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "👑", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = TrophyGold,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "الهبدة الفائزة بأعلى أصوات! ($highestVotes أصوات)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "« $winningBluffText »",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            lineHeight = 26.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (winningPlayer != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlayerAvatar(player = winningPlayer, size = 44.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "صاحب هذه الهبدة الأسطورية:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = winningPlayer.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // All Bluffs Breakdown
        Text(
            text = "كشف أصحاب كل الهبدات ومن صدقهم 🎭",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        uiState.players.forEach { player ->
            val bluff = uiState.playerBluffs[player.id] ?: ""
            val votersForThisPlayer = uiState.roundVotes[player.id] ?: emptyList()
            val voterNames = votersForThisPlayer.mapNotNull { voterId ->
                uiState.players.firstOrNull { it.id == voterId }?.name
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlayerAvatar(player = player, size = 36.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            color = if (votersForThisPlayer.isNotEmpty()) TruthGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${votersForThisPlayer.size} أصوات",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (votersForThisPlayer.isNotEmpty()) TruthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = bluff,
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )

                    if (voterNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "خدع كل من: ${voterNames.joinToString("، ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrophyGold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shocking Real Truth Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.showRealTruth) TruthGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    if (uiState.showRealTruth) 1.5.dp else 0.dp,
                    if (uiState.showRealTruth) TruthGreen else Color.Transparent,
                    RoundedCornerShape(18.dp)
                )
                .clickable { viewModel.toggleRealTruth() }
                .testTag("reveal_real_truth_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TruthGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🤯 الحقيقة الصادمة (الإجابة الحقيقية)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (uiState.showRealTruth) TruthGreen else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Icon(
                        imageVector = if (uiState.showRealTruth) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (uiState.showRealTruth) TruthGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = uiState.showRealTruth,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = question?.trueAnswer ?: "",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Next Round Actions
        Button(
            onClick = { viewModel.nextRound() },
            colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("next_round_button")
        ) {
            Text(
                text = "الجولة التالية ⬅️",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.endGame() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("end_game_button")
        ) {
            Text(text = "إنهاء الجلسة وتتويج الفائز النهائي 🏆")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
