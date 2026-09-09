package com.example.android.xo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.android.xo.R
import com.example.android.xo.data.GamePreferences
import com.example.android.xo.engine.GameMode
import com.example.android.xo.engine.Player

@Composable
fun HomeScreen(
    preferences: GamePreferences,
    onStartGame: (GameMode, Player) -> Unit,
    onOpenStats: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settings.collectAsStateWithLifecycle(initialValue = GamePreferences.Settings())
    var showSetup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.space_icon),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.app_name), style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
        Text(
            stringResource(R.string.app_tagline),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = { showSetup = true },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text(stringResource(R.string.mode_single_player)) }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onStartGame(GameMode.TWO_PLAYERS, Player.X) },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text(stringResource(R.string.mode_two_player)) }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onOpenStats) { Text(stringResource(R.string.stats_button)) }
            TextButton(onClick = onOpenAbout) { Text(stringResource(R.string.about_button)) }
        }
    }

    if (showSetup) {
        ComputerSetupDialog(
            initialSymbol = settings.preferredSymbol,
            onDismiss = { showSetup = false },
            onStart = { mode, player ->
                showSetup = false
                onStartGame(mode, player)
            },
            onSaveSymbol = { player ->
                scope.launch { preferences.setPreferredSymbol(player) }
            }
        )
    }
}

@Composable
private fun ComputerSetupDialog(
    initialSymbol: Player,
    onDismiss: () -> Unit,
    onStart: (GameMode, Player) -> Unit,
    onSaveSymbol: (Player) -> Unit
) {
    var difficulty by remember { mutableStateOf(GameMode.VS_AI_MEDIUM) }
    var symbol by remember(initialSymbol) { mutableStateOf(initialSymbol) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_ai_title)) },
        text = {
            Column {
                Text(stringResource(R.string.select_difficulty), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                listOf(
                    GameMode.VS_AI_EASY to R.string.difficulty_easy,
                    GameMode.VS_AI_MEDIUM to R.string.difficulty_medium,
                    GameMode.VS_AI_IMPOSSIBLE to R.string.difficulty_impossible
                ).forEach { (mode, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = difficulty == mode, onClick = { difficulty = mode })
                        Text(stringResource(label))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.select_symbol), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                listOf(Player.X to R.string.symbol_x_desc, Player.O to R.string.symbol_o_desc).forEach { (player, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = symbol == player, onClick = { symbol = player })
                        Text(stringResource(label))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSaveSymbol(symbol); onStart(difficulty, symbol) }) {
                Text(stringResource(R.string.start_game))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } }
    )
}
