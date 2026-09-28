package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.local.AppDatabase
import com.example.data.model.GameMode
import com.example.data.model.GameSettings
import com.example.data.model.Player
import com.example.data.model.Question
import com.example.data.model.QuestionCategory
import com.example.data.repository.GameRepository
import com.example.data.repository.QuestionsBank
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    HOME,
    PLAYERS,
    GAME_CONFIG,
    PLAY,
    GAME_OVER,
    CUSTOM_QUESTIONS,
    RULES
}

enum class RoundPhase {
    WRITING_HANDOFF,  // "مرر الهاتف إلى [اللاعب] لكتابة هبدته"
    WRITING_INPUT,    // Player writes their bluff secretly
    VOTING_HANDOFF,   // "مرر الهاتف إلى [المصوت] لاختيار أفضل هبدة"
    VOTING_INPUT,     // Voter votes for their favorite bluff (excluding their own!)
    ROUND_REVEAL      // Winning bluff revealed, who wrote it, vote tallies, and the shocking real truth!
}

data class AnonymousBluffOption(
    val ownerPlayerId: String,
    val text: String
)

data class GameUiState(
    val currentScreen: Screen = Screen.HOME,
    val previousScreen: Screen = Screen.HOME,
    val players: List<Player> = listOf(
        Player(id = "1", name = "أحمد", emoji = "😎", colorHex = 0xFF7C3AED),
        Player(id = "2", name = "سارة", emoji = "👑", colorHex = 0xFFEC4899),
        Player(id = "3", name = "كريم", emoji = "🎭", colorHex = 0xFFF59E0B)
    ),
    val settings: GameSettings = GameSettings(),
    val currentRound: Int = 1,
    val currentQuestion: Question? = null,

    // Round lifecycle states
    val roundPhase: RoundPhase = RoundPhase.WRITING_HANDOFF,
    val writingQueue: List<String> = emptyList(),
    val currentWriterIndex: Int = 0,
    val votingQueue: List<String> = emptyList(),
    val currentVoterIndex: Int = 0,

    // Collected bluffs for the round: playerId -> Bluff text
    val playerBluffs: Map<String, String> = emptyMap(),

    // Collected votes: targetPlayerId -> List of voter playerIds
    val roundVotes: Map<String, List<String>> = emptyMap(),

    // Cached shuffled options for current voter (excluding their own bluff)
    val currentVoterOptions: List<AnonymousBluffOption> = emptyList(),

    // Winner of the round
    val winningPlayerId: String? = null,
    val winningBluffText: String? = null,
    val highestVotesCount: Int = 0,
    val isTie: Boolean = false,

    // Timer
    val timerSecondsRemaining: Int = 45,
    val isTimerRunning: Boolean = false,
    val isTimeUp: Boolean = false,

    // Reveal options
    val showRealTruth: Boolean = false,
    val showBluffHint: Boolean = false,
    val winner: Player? = null
) {
    val currentWriter: Player?
        get() = players.firstOrNull { it.id == writingQueue.getOrNull(currentWriterIndex) }

    val currentVoter: Player?
        get() = players.firstOrNull { it.id == votingQueue.getOrNull(currentVoterIndex) }

    val winningPlayer: Player?
        get() = players.firstOrNull { it.id == winningPlayerId }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(AppDatabase.getInstance(application))
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val customQuestions: StateFlow<List<Question>> = repository.getAllCustomQuestions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var questionDeck: MutableList<Question> = mutableListOf()
    private var timerJob: Job? = null

    init {
        loadDeck()
    }

    private fun loadDeck() {
        questionDeck = repository.getQuestions(_uiState.value.settings.selectedCategories).toMutableList()
        if (questionDeck.isNotEmpty()) {
            _uiState.update { it.copy(currentQuestion = questionDeck.firstOrNull()) }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update {
            it.copy(
                previousScreen = it.currentScreen,
                currentScreen = screen
            )
        }
    }

    fun navigateBack() {
        val prev = _uiState.value.previousScreen
        val target = if (prev != _uiState.value.currentScreen && prev != Screen.PLAY) prev else Screen.HOME
        _uiState.update { it.copy(currentScreen = target) }
    }

    // --- Player Management ---
    fun addPlayer(name: String, emoji: String, colorHex: Long) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val newPlayer = Player(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            emoji = emoji,
            colorHex = colorHex
        )
        _uiState.update { state ->
            state.copy(players = state.players + newPlayer)
        }
    }

    fun removePlayer(id: String) {
        _uiState.update { state ->
            state.copy(players = state.players.filter { it.id != id })
        }
    }

    fun clearAllPlayers() {
        _uiState.update { it.copy(players = emptyList()) }
    }

    fun loadPreset(presetType: String) {
        val presetPlayers = when (presetType) {
            "friends" -> listOf(
                Player(id = "1", name = "عمرو", emoji = "🔥", colorHex = 0xFFEF4444),
                Player(id = "2", name = "مريم", emoji = "✨", colorHex = 0xFFEC4899),
                Player(id = "3", name = "زياد", emoji = "🦁", colorHex = 0xFFF59E0B),
                Player(id = "4", name = "ياسمين", emoji = "🌸", colorHex = 0xFF8B5CF6)
            )
            "family" -> listOf(
                Player(id = "1", name = "بابا", emoji = "👑", colorHex = 0xFF1E3A8A),
                Player(id = "2", name = "ماما", emoji = "❤️", colorHex = 0xFFEC4899),
                Player(id = "3", name = "خالد", emoji = "⚡", colorHex = 0xFF10B981),
                Player(id = "4", name = "هدى", emoji = "🌟", colorHex = 0xFFF59E0B)
            )
            else -> listOf(
                Player(id = "1", name = "البكّاس 1", emoji = "🎭", colorHex = 0xFF7C3AED),
                Player(id = "2", name = "الهبّاد 2", emoji = "🎲", colorHex = 0xFF06B6D4),
                Player(id = "3", name = "المقنع 3", emoji = "🧠", colorHex = 0xFF10B981)
            )
        }
        _uiState.update { it.copy(players = presetPlayers) }
    }

    // --- Settings ---
    fun updateSettings(newSettings: GameSettings) {
        _uiState.update { it.copy(settings = newSettings) }
        loadDeck()
    }

    fun toggleCategory(category: QuestionCategory) {
        _uiState.update { state ->
            val current = state.settings.selectedCategories.toMutableSet()
            if (category == QuestionCategory.ALL) {
                current.clear()
                current.add(QuestionCategory.ALL)
            } else {
                current.remove(QuestionCategory.ALL)
                if (current.contains(category)) {
                    current.remove(category)
                    if (current.isEmpty()) current.add(QuestionCategory.ALL)
                } else {
                    current.add(category)
                }
            }
            state.copy(settings = state.settings.copy(selectedCategories = current))
        }
        loadDeck()
    }

    fun toggleSound() {
        _uiState.update { state ->
            state.copy(settings = state.settings.copy(soundEnabled = !state.settings.soundEnabled))
        }
    }

    // --- Game Lifecycle ---
    fun startNewGame() {
        stopTimer()
        loadDeck()

        val activePlayers = _uiState.value.players
        if (activePlayers.size < 2) return

        val resetPlayers = activePlayers.map {
            it.copy(score = 0, bluffsSucceeded = 0, bluffsCaught = 0)
        }

        val firstQuestion = if (questionDeck.isNotEmpty()) {
            questionDeck.removeAt(0)
        } else {
            QuestionsBank.curatedQuestions.random()
        }

        // Randomize player order for writing
        val randomizedWriters = resetPlayers.map { it.id }.shuffled()

        _uiState.update { state ->
            state.copy(
                players = resetPlayers,
                currentRound = 1,
                currentQuestion = firstQuestion,
                roundPhase = RoundPhase.WRITING_HANDOFF,
                writingQueue = randomizedWriters,
                currentWriterIndex = 0,
                votingQueue = emptyList(),
                currentVoterIndex = 0,
                playerBluffs = emptyMap(),
                roundVotes = emptyMap(),
                currentVoterOptions = emptyList(),
                winningPlayerId = null,
                winningBluffText = null,
                highestVotesCount = 0,
                isTie = false,
                timerSecondsRemaining = state.settings.roundTimerSeconds,
                isTimerRunning = false,
                isTimeUp = false,
                showRealTruth = false,
                showBluffHint = false,
                winner = null,
                currentScreen = Screen.PLAY
            )
        }
    }

    // --- Phase 1: Bluff Writing Flow ---
    fun startWritingForCurrentPlayer() {
        _uiState.update {
            it.copy(
                roundPhase = RoundPhase.WRITING_INPUT,
                timerSecondsRemaining = it.settings.roundTimerSeconds,
                isTimeUp = false,
                showBluffHint = false
            )
        }
        if (_uiState.value.settings.roundTimerSeconds > 0) {
            startTimer()
        }
    }

    fun submitPlayerBluff(bluffText: String) {
        stopTimer()
        val textToSave = bluffText.trim().ifEmpty { "إجابة غامضة لا يمكن تصديقها!" }
        val currentWriterId = _uiState.value.writingQueue.getOrNull(_uiState.value.currentWriterIndex) ?: return

        val updatedBluffs = _uiState.value.playerBluffs.toMutableMap()
        updatedBluffs[currentWriterId] = textToSave

        soundManager.playSuccess(_uiState.value.settings.soundEnabled, _uiState.value.settings.hapticsEnabled)

        val nextWriterIndex = _uiState.value.currentWriterIndex + 1

        if (nextWriterIndex < _uiState.value.writingQueue.size) {
            // Still more players need to write bluffs
            _uiState.update {
                it.copy(
                    playerBluffs = updatedBluffs,
                    currentWriterIndex = nextWriterIndex,
                    roundPhase = RoundPhase.WRITING_HANDOFF,
                    timerSecondsRemaining = it.settings.roundTimerSeconds,
                    isTimeUp = false,
                    showBluffHint = false
                )
            }
        } else {
            // All players finished writing bluffs! Begin voting phase
            // Randomize voting order among all players
            val randomizedVoters = _uiState.value.players.map { it.id }.shuffled()
            _uiState.update {
                it.copy(
                    playerBluffs = updatedBluffs,
                    roundPhase = RoundPhase.VOTING_HANDOFF,
                    votingQueue = randomizedVoters,
                    currentVoterIndex = 0,
                    roundVotes = emptyMap(),
                    timerSecondsRemaining = it.settings.roundTimerSeconds,
                    isTimeUp = false
                )
            }
        }
    }

    // --- Phase 2: Voting Flow ---
    fun startVotingForCurrentPlayer() {
        val currentVoterId = _uiState.value.votingQueue.getOrNull(_uiState.value.currentVoterIndex) ?: return
        val allBluffs = _uiState.value.playerBluffs

        // Crucial rule from user:
        // "ميقدرش يختار اجابته بس يقدر يختار اي اجابه من الي موجدين غير اجابته هو"
        val optionsForThisVoter = allBluffs
            .filter { (ownerId, _) -> ownerId != currentVoterId }
            .map { (ownerId, text) -> AnonymousBluffOption(ownerPlayerId = ownerId, text = text) }
            .shuffled() // Shuffle so nobody guesses position

        _uiState.update {
            it.copy(
                roundPhase = RoundPhase.VOTING_INPUT,
                currentVoterOptions = optionsForThisVoter,
                timerSecondsRemaining = it.settings.roundTimerSeconds,
                isTimeUp = false
            )
        }

        if (_uiState.value.settings.roundTimerSeconds > 0) {
            startTimer()
        }
    }

    fun submitVote(chosenOwnerPlayerId: String) {
        stopTimer()
        val currentVoterId = _uiState.value.votingQueue.getOrNull(_uiState.value.currentVoterIndex) ?: return

        val currentVotes = _uiState.value.roundVotes.toMutableMap()
        val existingVoters = currentVotes[chosenOwnerPlayerId]?.toMutableList() ?: mutableListOf()
        if (!existingVoters.contains(currentVoterId)) {
            existingVoters.add(currentVoterId)
        }
        currentVotes[chosenOwnerPlayerId] = existingVoters

        soundManager.playSuccess(_uiState.value.settings.soundEnabled, _uiState.value.settings.hapticsEnabled)

        val nextVoterIndex = _uiState.value.currentVoterIndex + 1

        if (nextVoterIndex < _uiState.value.votingQueue.size) {
            // More players need to vote
            _uiState.update {
                it.copy(
                    roundVotes = currentVotes,
                    currentVoterIndex = nextVoterIndex,
                    roundPhase = RoundPhase.VOTING_HANDOFF,
                    timerSecondsRemaining = it.settings.roundTimerSeconds,
                    isTimeUp = false
                )
            }
        } else {
            // All players voted! Calculate results and reveal winner
            calculateAndRevealWinner(currentVotes)
        }
    }

    private fun calculateAndRevealWinner(finalVotes: Map<String, List<String>>) {
        // Tally votes per player
        val bluffs = _uiState.value.playerBluffs

        var maxVotes = 0
        var topBlufferId: String? = null
        var isTie = false

        // Count votes
        val voteCounts = mutableMapOf<String, Int>()
        _uiState.value.players.forEach { p ->
            val votesReceived = finalVotes[p.id]?.size ?: 0
            voteCounts[p.id] = votesReceived
            if (votesReceived > maxVotes) {
                maxVotes = votesReceived
                topBlufferId = p.id
                isTie = false
            } else if (votesReceived == maxVotes && maxVotes > 0) {
                isTie = true
            }
        }

        // If no one got votes (e.g. 0 votes), pick random or first bluffer
        if (topBlufferId == null && _uiState.value.players.isNotEmpty()) {
            topBlufferId = _uiState.value.players.first().id
        }

        // Award points: +3 points per vote received for your bluff!
        // Plus +5 bonus points for the player with the most convincing bluff!
        val updatedPlayers = _uiState.value.players.map { player ->
            val received = finalVotes[player.id]?.size ?: 0
            val isTopWinner = (player.id == topBlufferId) && maxVotes > 0
            val bonusPoints = if (isTopWinner) 5 else 0
            val roundPoints = (received * 3) + bonusPoints

            player.copy(
                score = player.score + roundPoints,
                bluffsSucceeded = player.bluffsSucceeded + (if (received > 0) 1 else 0)
            )
        }

        val topBluffText = topBlufferId?.let { bluffs[it] } ?: ""

        soundManager.playSuccess(_uiState.value.settings.soundEnabled, _uiState.value.settings.hapticsEnabled)

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                roundVotes = finalVotes,
                winningPlayerId = topBlufferId,
                winningBluffText = topBluffText,
                highestVotesCount = maxVotes,
                isTie = isTie,
                roundPhase = RoundPhase.ROUND_REVEAL,
                showRealTruth = false
            )
        }
    }

    fun toggleRealTruth() {
        val willShow = !_uiState.value.showRealTruth
        _uiState.update { it.copy(showRealTruth = willShow) }
        if (willShow) {
            soundManager.playSuccess(_uiState.value.settings.soundEnabled, _uiState.value.settings.hapticsEnabled)
        }
    }

    fun toggleBluffHint() {
        _uiState.update { it.copy(showBluffHint = !it.showBluffHint) }
    }

    fun nextRound() {
        stopTimer()
        val state = _uiState.value

        // Check if finished total rounds
        if (state.settings.totalRounds > 0 && state.currentRound >= state.settings.totalRounds) {
            endGame()
            return
        }

        if (questionDeck.isEmpty()) {
            loadDeck()
        }
        val nextQ = if (questionDeck.isNotEmpty()) questionDeck.removeAt(0) else QuestionsBank.curatedQuestions.random()

        // Randomize player order for the next round
        val randomizedWriters = state.players.map { it.id }.shuffled()

        _uiState.update {
            it.copy(
                currentRound = it.currentRound + 1,
                currentQuestion = nextQ,
                roundPhase = RoundPhase.WRITING_HANDOFF,
                writingQueue = randomizedWriters,
                currentWriterIndex = 0,
                votingQueue = emptyList(),
                currentVoterIndex = 0,
                playerBluffs = emptyMap(),
                roundVotes = emptyMap(),
                currentVoterOptions = emptyList(),
                winningPlayerId = null,
                winningBluffText = null,
                highestVotesCount = 0,
                isTie = false,
                timerSecondsRemaining = it.settings.roundTimerSeconds,
                isTimerRunning = false,
                isTimeUp = false,
                showRealTruth = false,
                showBluffHint = false
            )
        }
    }

    fun endGame() {
        stopTimer()
        val state = _uiState.value
        val winner = state.players.maxByOrNull { it.score }
        soundManager.playSuccess(state.settings.soundEnabled, state.settings.hapticsEnabled)

        _uiState.update {
            it.copy(
                winner = winner,
                currentScreen = Screen.GAME_OVER
            )
        }
    }

    // --- Timer ---
    fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = true, isTimeUp = false) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSecondsRemaining > 0 && _uiState.value.isTimerRunning) {
                delay(1000)
                val remaining = _uiState.value.timerSecondsRemaining - 1
                _uiState.update { it.copy(timerSecondsRemaining = remaining) }

                if (remaining in 1..5) {
                    soundManager.playUrgentTick(
                        _uiState.value.settings.soundEnabled,
                        _uiState.value.settings.hapticsEnabled
                    )
                } else if (remaining > 5) {
                    soundManager.playTick(
                        _uiState.value.settings.soundEnabled,
                        _uiState.value.settings.hapticsEnabled
                    )
                }
            }

            if (_uiState.value.timerSecondsRemaining <= 0) {
                _uiState.update { it.copy(isTimerRunning = false, isTimeUp = true) }
                soundManager.playTimeUp(
                    _uiState.value.settings.soundEnabled,
                    _uiState.value.settings.hapticsEnabled
                )
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timerSecondsRemaining = it.settings.roundTimerSeconds,
                isTimerRunning = false,
                isTimeUp = false
            )
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // --- Custom Questions ---
    fun addCustomQuestion(text: String, trueAnswer: String, bluffHint: String) {
        viewModelScope.launch {
            repository.addCustomQuestion(text, trueAnswer, bluffHint)
        }
    }

    fun deleteCustomQuestion(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomQuestion(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        soundManager.release()
    }
}
