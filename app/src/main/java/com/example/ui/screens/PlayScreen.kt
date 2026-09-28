package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticFeedbackManager
import com.example.audio.SoundPlayer
import com.example.data.AppRepository
import com.example.engine.RulesEngine
import com.example.engine.ScoreBreakdown
import com.example.engine.ScoreSystem
import com.example.levels.LevelRepository
import com.example.model.EngineEvent
import com.example.model.EngineState
import com.example.model.GameStatus
import com.example.model.LevelData
import com.example.model.TutorialStep
import com.example.ui.components.LoseDialog
import com.example.ui.components.PassengerView
import com.example.ui.components.PauseDialog
import com.example.ui.components.SlotView
import com.example.ui.components.TutorialOverlay
import com.example.ui.components.VehicleView
import com.example.ui.components.VirtualTourDialog
import com.example.ui.components.WinDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    level: LevelData,
    repository: AppRepository,
    onBackToLevels: () -> Unit,
    onNextLevel: (LevelData) -> Unit,
    onOpenSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    val hapticEnabled by repository.hapticEnabled.collectAsState()

    LaunchedEffect(hapticEnabled) {
        HapticFeedbackManager.isEnabled = hapticEnabled
    }

    var currentState by remember(level.id) {
        mutableStateOf(RulesEngine.createState(level))
    }

    // Undo stack keeping prior immutable states
    val undoStack = remember(level.id) {
        mutableStateListOf<EngineState>()
    }

    var isPaused by remember { mutableStateOf(false) }
    var blockedVehicleId by remember { mutableStateOf<String?>(null) }
    var lastAnnouncement by remember { mutableStateOf("") }

    // Active tutorial tip
    var currentTutorialStep by remember(level.id) {
        mutableStateOf(
            when (level.id) {
                "L001" -> TutorialStep.TAP_TO_MOVE
                "L002" -> TutorialStep.BLOCKED_MOVE
                "L003" -> TutorialStep.FULL_SLOTS
                else -> null
            }
        )
    }

    // Zoom and pan state for dense boards
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Win / Lose dialogs
    var showWinDialog by remember { mutableStateOf(false) }
    var showLoseDialog by remember { mutableStateOf(false) }
    var showTourDialog by remember { mutableStateOf(false) }

    // Scoring and live speed/move tracking
    var elapsedSeconds by remember(level.id) { mutableIntStateOf(0) }
    var winScoreBreakdown by remember { mutableStateOf<ScoreBreakdown?>(null) }
    var newlyUnlockedLevelData by remember(level.id) { mutableStateOf<LevelData?>(null) }
    var previousHighScore by remember(level.id) { mutableIntStateOf(0) }

    LaunchedEffect(level.id) {
        previousHighScore = repository.getHighScoreForLevel(level.id)
    }

    LaunchedEffect(level.id, isPaused, currentState.status) {
        if (currentState.status == GameStatus.PLAYING && !isPaused) {
            while (isActive && currentState.status == GameStatus.PLAYING && !isPaused) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    val departedTrotrosCount = remember(currentState.carPark.size, currentState.slots) {
        (level.vehicles.size - (currentState.carPark.size + currentState.slots.count { it != null })).coerceAtLeast(0)
    }

    val liveScore = remember(currentState.moves, elapsedSeconds, currentState.totalPassengersRemaining, departedTrotrosCount) {
        ScoreSystem.calculateLiveScore(
            moves = currentState.moves,
            par = level.par,
            elapsedSeconds = elapsedSeconds,
            totalPassengers = level.queue.size,
            passengersRemaining = currentState.totalPassengersRemaining,
            trotrosDeparted = departedTrotrosCount
        )
    }

    // Computes which vehicles have a clear exit path and can leave right now
    val legalVehicleIds = remember(currentState) {
        RulesEngine.legalActions(currentState).toSet()
    }

    // Back handler opens pause menu or navigates
    BackHandler {
        if (!isPaused && currentState.status == GameStatus.PLAYING) {
            isPaused = true
        } else {
            onBackToLevels()
        }
    }

    fun handleVehicleClick(vehicleId: String) {
        if (currentState.status != GameStatus.PLAYING) return
        SoundPlayer.playClick()

        val (nextState, events) = RulesEngine.applyAction(currentState, vehicleId)

        // Process audio and announcements for each event
        for (event in events) {
            when (event) {
                is EngineEvent.Moved -> {
                    // Record previous state for unlimited undo
                    undoStack.add(currentState)
                    SoundPlayer.playMove()
                    HapticFeedbackManager.performMoveSuccess()
                    if (hapticEnabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    lastAnnouncement = "Trotro moved to slot ${event.slotIndex + 1}"
                }
                is EngineEvent.Blocked -> {
                    blockedVehicleId = event.vehicleId
                    SoundPlayer.playBlocked()
                    HapticFeedbackManager.performObstacleHit()
                    if (hapticEnabled) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    lastAnnouncement = "Exit path is blocked by another vehicle!"
                    scope.launch {
                        snackbarHostState.showSnackbar("Path Blocked! Trotro cannot exit.")
                    }
                }
                is EngineEvent.NoFreeSlot -> {
                    SoundPlayer.playNoSlot()
                    HapticFeedbackManager.performNoSlot()
                    lastAnnouncement = "No free parking slots available!"
                    scope.launch {
                        snackbarHostState.showSnackbar("All parking slots full!")
                    }
                }
                is EngineEvent.Boarded -> {
                    SoundPlayer.playBoard()
                    HapticFeedbackManager.performBoard()
                    lastAnnouncement = "${event.colour.displayName} passenger boarded slot ${event.slotIndex + 1}"
                }
                is EngineEvent.Departed -> {
                    SoundPlayer.playDepart()
                    HapticFeedbackManager.performMoveSuccess()
                    lastAnnouncement = "Trotro in slot ${event.slotIndex + 1} departed!"
                }
                is EngineEvent.Won -> {
                    SoundPlayer.playWin()
                    HapticFeedbackManager.performWin()
                    lastAnnouncement = "Station Clear! You won the level!"
                    val finalBreakdown = ScoreSystem.calculateFinalScore(
                        moves = nextState.moves,
                        par = level.par,
                        elapsedSeconds = elapsedSeconds,
                        totalPassengers = level.queue.size,
                        existingHighScore = previousHighScore
                    )
                    winScoreBreakdown = finalBreakdown
                    scope.launch {
                        val unlockedId = repository.recordWin(level.id, nextState.moves, level.par, finalBreakdown.totalScore)
                        if (unlockedId != null) {
                            newlyUnlockedLevelData = LevelRepository.getLevelById(unlockedId)
                        }
                        previousHighScore = maxOf(previousHighScore, finalBreakdown.totalScore)
                    }
                    showWinDialog = true
                }
                is EngineEvent.Lost -> {
                    SoundPlayer.playLose()
                    lastAnnouncement = "Traffic Jam! No legal moves remain."
                    showLoseDialog = true
                }
            }
        }

        currentState = nextState
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            currentState = previous
            blockedVehicleId = null
            showLoseDialog = false
            SoundPlayer.playMove()
            HapticFeedbackManager.performMoveSuccess()
            if (hapticEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            lastAnnouncement = "Move undone. Restored previous game state. Move count: ${previous.moves}."
            scope.launch {
                snackbarHostState.showSnackbar("Move undone · Moves: ${previous.moves} / Par: ${level.par}")
            }
        }
    }

    fun handleRestart() {
        undoStack.clear()
        currentState = RulesEngine.createState(level)
        elapsedSeconds = 0
        winScoreBreakdown = null
        newlyUnlockedLevelData = null
        showWinDialog = false
        showLoseDialog = false
        isPaused = false
        SoundPlayer.playMove()
        lastAnnouncement = "Level restarted."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${level.id}: ${level.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = level.difficultyTier.badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = level.difficultyTier.name,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = level.difficultyTier.badgeColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏆", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "%,d".format(liveScore),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Moves: ${currentState.moves} / Par: ${level.par}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Time: ${ScoreSystem.formatTime(elapsedSeconds)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToLevels) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Levels")
                    }
                },
                actions = {
                    IconButton(
                        onClick = ::handleUndo,
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.testTag("undo_appbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = if (undoStack.isNotEmpty()) "Undo last move (${undoStack.size} available)" else "Undo disabled",
                            tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
                        )
                    }
                    IconButton(onClick = { showTourDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Station Tour & Rules")
                    }
                    IconButton(onClick = { isPaused = true }) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause game")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom Action Bar: Undo, Restart, and Accessibility Summary
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = ::handleUndo,
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier
                            .testTag("undo_button")
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (undoStack.isNotEmpty()) "Undo (${undoStack.size})" else "Undo",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = ::handleRestart,
                        modifier = Modifier
                            .testTag("restart_button")
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restart")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live region announcement for TalkBack / Screen Readers (REQ-A11Y-006)
            Box(
                modifier = Modifier
                    .size(1.dp)
                    .semantics {
                        liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                    }
            ) {
                Text(text = lastAnnouncement)
            }

            // Tutorial Hint if active
            currentTutorialStep?.let { step ->
                TutorialOverlay(
                    step = step,
                    onDismiss = { currentTutorialStep = null }
                )
            }

            // TOP SCORING & SPEED HUD BAR (Displayed prominently at the top of the screen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("score_hud"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LIVE SCORE DISPLAY
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("score_display")
                    ) {
                        Text(text = "🏆", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "LIVE SCORE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%,d".format(liveScore),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    // SPEED & CLEARANCE TIME DISPLAY
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("speed_display")
                    ) {
                        Text(text = "⏱️", fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val targetSec = ScoreSystem.calculateTargetSeconds(level.par)
                            val isSpeedy = elapsedSeconds <= targetSec
                            Text(
                                text = "SPEED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ScoreSystem.formatTime(elapsedSeconds),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSpeedy) Color(0xFF10B981) else Color(0xFFF97316)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSpeedy) "⚡" else "⏳",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // MOVES / PAR
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("moves_display")
                    ) {
                        val moveColor = when {
                            currentState.moves <= level.par -> Color(0xFF10B981)
                            currentState.moves <= level.par + 2 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
                        Text(text = "🎯", fontSize = 17.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "MOVES / PAR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${currentState.moves} / ${level.par}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = moveColor
                            )
                        }
                    }
                }
            }

            // 1. PASSENGER QUEUE SECTION (Figure 1)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PASSENGER QUEUE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${currentState.totalPassengersRemaining} Waiting",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Queue line with Gate on the right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Show first 10 passengers in queue order (head is first)
                        currentState.queue.take(10).forEachIndexed { index, colour ->
                            if (index == 0) {
                                // Head passenger badge
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "GATE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    PassengerView(colour = colour, size = 36.dp)
                                }
                                Text(
                                    text = " ◀ ",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            } else {
                                PassengerView(
                                    colour = colour,
                                    size = 30.dp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }

                        if (currentState.queue.size > 10) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+${currentState.queue.size - 10}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (currentState.queue.isEmpty()) {
                            Text(
                                text = "Queue is empty! All passengers boarded!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 2. PARKING SLOTS SECTION (Figure 1)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PARKING BAYS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${currentState.freeSlotsCount}/${currentState.slots.size} Free",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (currentState.freeSlotsCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentState.slots.forEachIndexed { index, vehicle ->
                            SlotView(
                                slotIndex = index,
                                vehicle = vehicle,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. CAR PARK GRID WITH ZOOM/PAN (Figure 1)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Traffic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LORRY PARK YARD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${currentState.carPark.size} Jammed · ${legalVehicleIds.size} Can Exit ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (legalVehicleIds.isNotEmpty()) Color(0xFF22C55E) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    val containerWidth = maxWidth
                    val containerHeight = maxHeight

                    // Calculate ideal cell size so board fits or scales
                    val baseCellWidth = containerWidth / level.gridCols
                    val baseCellHeight = containerHeight / level.gridRows
                    val baseCellSize = minOf(baseCellWidth, baseCellHeight).coerceAtLeast(38.dp)
                    val effectiveCellSize = baseCellSize * zoomScale

                    val boardWidth = effectiveCellSize * level.gridCols
                    val boardHeight = effectiveCellSize * level.gridRows

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(0.7f, 2.2f)
                                    panOffsetX += pan.x
                                    panOffsetY += pan.y
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(panOffsetX.dp, panOffsetY.dp)
                                .size(boardWidth, boardHeight)
                        ) {
                            // Draw grid lines / asphalt markings
                            for (r in 0 until level.gridRows) {
                                for (c in 0 until level.gridCols) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = effectiveCellSize * c, y = effectiveCellSize * r)
                                            .size(effectiveCellSize)
                                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    )
                                }
                            }

                            // Render Jammed Vehicles on the Car Park
                            currentState.carPark.forEach { vehicle ->
                                val isBlocked = (blockedVehicleId == vehicle.id)
                                val isClear = legalVehicleIds.contains(vehicle.id)
                                VehicleView(
                                    vehicle = vehicle,
                                    cellSize = effectiveCellSize,
                                    isBlocked = isBlocked,
                                    isClearToExit = isClear,
                                    modifier = Modifier.offset(
                                        x = effectiveCellSize * vehicle.col,
                                        y = effectiveCellSize * vehicle.row
                                    ),
                                    onClick = { handleVehicleClick(vehicle.id) }
                                )
                            }
                        }
                    }

                    // Floating Zoom Controls (REQ-UI-008)
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.7f) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                            }
                            IconButton(
                                onClick = {
                                    zoomScale = 1.0f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.CropFree, contentDescription = "Fit Board")
                            }
                            IconButton(
                                onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.2f) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In")
                            }
                        }
                    }
                }
            }
        }
    }

    // Win Dialog
    if (showWinDialog) {
        val stars = when {
            currentState.moves <= level.par -> 3
            currentState.moves <= level.par + 2 -> 2
            else -> 1
        }
        WinDialog(
            moves = currentState.moves,
            par = level.par,
            stars = stars,
            scoreBreakdown = winScoreBreakdown,
            unlockedNextLevel = newlyUnlockedLevelData,
            onNextLevel = {
                showWinDialog = false
                val next = com.example.levels.LevelRepository.levels.getOrNull(
                    com.example.levels.LevelRepository.levels.indexOfFirst { it.id == level.id } + 1
                )
                if (next != null) onNextLevel(next) else onBackToLevels()
            },
            onReplay = {
                showWinDialog = false
                handleRestart()
            },
            onLevelSelect = onBackToLevels
        )
    }

    // Lose Dialog
    if (showLoseDialog) {
        LoseDialog(
            onUndo = {
                showLoseDialog = false
                handleUndo()
            },
            onRestart = {
                showLoseDialog = false
                handleRestart()
            },
            onLevelSelect = onBackToLevels
        )
    }

    // Pause Dialog
    if (isPaused) {
        PauseDialog(
            onResume = { isPaused = false },
            onRestart = {
                isPaused = false
                handleRestart()
            },
            onLevelSelect = onBackToLevels,
            onSettings = {
                isPaused = false
                onOpenSettings()
            }
        )
    }

    // Virtual Tour Dialog
    if (showTourDialog) {
        VirtualTourDialog(
            onDismiss = { showTourDialog = false }
        )
    }
}
