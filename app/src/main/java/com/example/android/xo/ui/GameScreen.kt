package com.example.android.xo.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.xo.GameBoardViewModel
import com.example.android.xo.GameEffect
import com.example.android.xo.GameEvent
import com.example.android.xo.GameUiState
import com.example.android.xo.R
import com.example.android.xo.UiCell
import com.example.android.xo.UiResult
import com.example.android.xo.ads.BannerAdView
import com.example.android.xo.audio.HapticManager
import com.example.android.xo.engine.Player
import com.example.android.xo.ui.theme.XoGameColors
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameBoardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showResult by remember { mutableStateOf(false) }
    var winningIndices by remember { mutableStateOf<IntArray?>(null) }
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = remember(context) { HapticManager(context) }
    LaunchedEffect(uiState.isHapticEnabled) {
        haptic.isHapticEnabled = uiState.isHapticEnabled
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GameEffect.MoveMade -> {
                    haptic.performTap(view)
                }
                GameEffect.ShowResult -> showResult = true
                GameEffect.HideResult -> {
                    showResult = false
                    winningIndices = null
                }
                is GameEffect.WinLine -> {
                    winningIndices = effect.indices
                    haptic.performWin()
                }
                is GameEffect.SoundChanged -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(gameModeTitle(viewModel.gameMode)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.exit_to_menu)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(GameEvent.ToggleSound) }) {
                        Icon(
                            painter = painterResource(
                                if (uiState.isSoundEnabled) R.drawable.ic_sound_on else R.drawable.ic_sound_off
                            ),
                            contentDescription = stringResource(R.string.sound_toggle)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        GameContent(
            state = uiState,
            isAiMode = viewModel.gameMode.isAiMode,
            humanPlayer = viewModel.humanPlayer,
            winningIndices = winningIndices,
            onCellClick = { viewModel.onEvent(GameEvent.CellClicked(it)) },
            onResetScore = { viewModel.onEvent(GameEvent.ResetScore) },
            modifier = Modifier.padding(padding)
        )
    }

    if (showResult) {
        ResultDialog(result = uiState.result)
    }
}

@Composable
private fun GameContent(
    state: GameUiState,
    isAiMode: Boolean,
    humanPlayer: Player,
    winningIndices: IntArray?,
    onCellClick: (Int) -> Unit,
    onResetScore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TurnCard(state)
        Spacer(Modifier.height(16.dp))
        ScoreBoard(state, isAiMode, humanPlayer)

        Spacer(Modifier.height(16.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .wrapContentHeight(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            GameBoard(
                state = state,
                winningIndices = winningIndices,
                onCellClick = onCellClick,
                modifier = Modifier.size(boardSize).aspectRatio(1f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BannerAdView(modifier = Modifier.weight(1f).height(54.dp))
            OutlinedButton(onClick = onResetScore, modifier = Modifier.weight(1f).height(54.dp)) {
                Text(stringResource(R.string.reset_scores))
            }
        }
    }
}

@Composable
private fun TurnCard(state: GameUiState) {
    val isOver = state.result !is UiResult.InProgress
    val color = when {
        isOver -> XoGameColors.draw
        state.activePlayer == Player.X -> XoGameColors.x
        else -> XoGameColors.o
    }
    val label = when (val result = state.result) {
        UiResult.InProgress -> if (state.activePlayer == Player.X) stringResource(R.string.turn_x) else stringResource(R.string.turn_o)
        UiResult.Draw -> stringResource(R.string.game_draw)
        is UiResult.Winner -> if (result.isHumanWinner) stringResource(R.string.winner_you) else stringResource(R.string.winner_ai)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(label, Modifier.padding(start = 10.dp).weight(1f), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.app_tagline), fontSize = 11.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ScoreBoard(state: GameUiState, isAiMode: Boolean, humanPlayer: Player) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreCard(
            if (isAiMode && humanPlayer == Player.X) stringResource(R.string.score_you) else stringResource(R.string.cell_x),
            state.xScore,
            XoGameColors.x,
            Modifier.weight(1f)
        )
        ScoreCard(stringResource(R.string.score_draws), state.drawScore, XoGameColors.draw, Modifier.weight(1f))
        ScoreCard(
            if (isAiMode && humanPlayer == Player.O) stringResource(R.string.score_you)
            else if (isAiMode) stringResource(R.string.score_ai)
            else stringResource(R.string.cell_o),
            state.oScore,
            XoGameColors.o,
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreCard(label: String, score: Int, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accent),
        shape = androidx.compose.material3.MaterialTheme.shapes.small
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = accent, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            AnimatedContent(targetState = score, label = "score") { value ->
                Text(value.toString(), style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GameBoard(
    state: GameUiState,
    winningIndices: IntArray?,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = XoGameColors.board,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Box(Modifier.fillMaxSize().padding(8.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                state.board.chunked(3).forEachIndexed { row, cells ->
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        cells.forEachIndexed { column, cell ->
                            GameCell(
                                cell = cell,
                                index = row * 3 + column,
                                enabled = !state.isInputLocked && state.result is UiResult.InProgress,
                                modifier = Modifier.weight(1f).fillMaxSize(),
                                onClick = onCellClick
                            )
                        }
                    }
                }
            }
            WinningLine(
                player = state.result.winnerOrNull(),
                indices = winningIndices,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun GameCell(
    cell: UiCell,
    index: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit
) {
    val scale by animateFloatAsState(if (cell.isEmpty) 1f else 1f, tween(220), label = "cellScale")
    val symbol = cell.player?.name ?: ""
    val description = if (cell.player == null) {
        stringResource(R.string.cell_description, index + 1, stringResource(R.string.cell_empty))
    } else {
        stringResource(R.string.cell_description, index + 1, symbol)
    }
    val cellStateDescription = if (cell.isEmpty) stringResource(R.string.cell_empty) else symbol
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(XoGameColors.boardCell)
            .clickable(enabled = enabled && cell.isEmpty, role = Role.Button) { onClick(index) }
            .semantics {
                contentDescription = description
                role = Role.Button
                stateDescription = cellStateDescription
            }
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = cell.player, animationSpec = tween(180), label = "cellSymbol") { player ->
            when (player) {
                Player.X -> Text(
                    "X",
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
                    color = XoGameColors.x,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold
                )
                Player.O -> Text(
                    "O",
                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
                    color = XoGameColors.o,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold
                )
                null -> Spacer(Modifier.size(1.dp))
            }
        }
    }
}

@Composable
private fun WinningLine(player: Player?, indices: IntArray?, modifier: Modifier = Modifier) {
    if (indices == null || indices.size < 3) return
    val progress by animateFloatAsState(1f, tween(320), label = "winningLine")
    Canvas(modifier = modifier.padding(14.dp)) {
        val first = indices.first()
        val last = indices.last()
        val start = cellCenter(first, size.width, size.height)
        val end = cellCenter(last, size.width, size.height)
        drawLine(
            color = when (player) {
                Player.X -> XoGameColors.x
                Player.O -> XoGameColors.o
                null -> XoGameColors.draw
            },
            start = start,
            end = androidx.compose.ui.geometry.Offset(
                start.x + (end.x - start.x) * progress,
                start.y + (end.y - start.y) * progress
            ),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun UiResult.winnerOrNull(): Player? = (this as? UiResult.Winner)?.player

private fun cellCenter(index: Int, width: Float, height: Float): androidx.compose.ui.geometry.Offset {
    val col = index % 3
    val row = index / 3
    return androidx.compose.ui.geometry.Offset((col + 0.5f) * width / 3f, (row + 0.5f) * height / 3f)
}

@Composable
private fun ResultDialog(result: UiResult) {
    val (title, message, badge) = when (result) {
        UiResult.Draw -> Triple(stringResource(R.string.game_draw), stringResource(R.string.result_draw_message), "=")
        is UiResult.Winner -> if (result.isHumanWinner) {
            Triple(stringResource(R.string.winner_you), stringResource(R.string.result_win_message), result.player.name)
        } else {
            Triple(stringResource(R.string.winner_ai), stringResource(R.string.result_loss_message), result.player.name)
        }
        UiResult.InProgress -> return
    }
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(XoGameColors.draw), contentAlignment = Alignment.Center) {
                    Text(badge, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            }
        },
        text = { Text(message, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
    )
}

@Composable
private fun gameModeTitle(mode: com.example.android.xo.engine.GameMode): String = when (mode) {
    com.example.android.xo.engine.GameMode.TWO_PLAYERS -> stringResource(R.string.mode_title_two_players)
    com.example.android.xo.engine.GameMode.VS_AI_EASY -> stringResource(R.string.mode_title_ai, stringResource(R.string.difficulty_easy))
    com.example.android.xo.engine.GameMode.VS_AI_MEDIUM -> stringResource(R.string.mode_title_ai, stringResource(R.string.difficulty_medium))
    com.example.android.xo.engine.GameMode.VS_AI_IMPOSSIBLE -> stringResource(R.string.mode_title_ai, stringResource(R.string.difficulty_impossible))
}
