package com.example.android.xo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.android.xo.R
import com.example.android.xo.data.GamePreferences

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(preferences: GamePreferences, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val stats by preferences.statistics.collectAsStateWithLifecycle(initialValue = GamePreferences.Statistics())
    var confirmReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = { XoTopBar(stringResource(R.string.stats_title), onBack) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(stringResource(R.string.stats_streaks), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatValue(stats.currentStreak, stringResource(R.string.current_streak))
                        StatValue(stats.bestStreak, stringResource(R.string.best_streak))
                    }
                }
            }
            StatsCard(stringResource(R.string.stats_vs_ai), listOf(
                stringResource(R.string.stats_mode_easy) to stats.easy,
                stringResource(R.string.stats_mode_medium) to stats.medium,
                stringResource(R.string.stats_mode_impossible) to stats.impossible
            ))
            Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(stringResource(R.string.stats_two_player), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(
                            R.string.stats_two_player_values,
                            stats.twoPlayer.xWins,
                            stats.twoPlayer.oWins,
                            stats.twoPlayer.draws
                        ),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text(stringResource(R.string.stats_reset_button))
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.stats_reset_confirm_title)) },
            text = { Text(stringResource(R.string.stats_reset_confirm_msg)) },
            confirmButton = {
                Button(onClick = { confirmReset = false; scope.launch { preferences.resetStatistics() } }) {
                    Text(stringResource(R.string.action_reset))
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(android.R.string.cancel)) } }
        )
    }
}

@Composable
private fun StatsCard(title: String, rows: List<Pair<String, GamePreferences.ModeStats>>) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            rows.forEach { (mode, stats) ->
                Spacer(Modifier.height(14.dp))
                Text(mode, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.stats_wins, stats.wins) + "  ·  " +
                        stringResource(R.string.stats_losses, stats.losses) + "  ·  " +
                        stringResource(R.string.stats_draws, stats.draws) + "  ·  " +
                        stringResource(R.string.stats_win_rate, stats.winRatePercent),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatValue(value: Int, label: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value.toString(), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Text(label, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
