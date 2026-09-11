package ir.sharif.xo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.sharif.xo.BuildConfig
import ir.sharif.xo.R

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier, topBar = { XoTopBar(stringResource(R.string.about_title), onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            Text(stringResource(R.string.app_name), style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
            Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME), color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.padding(8.dp))
            AboutCard(stringResource(R.string.how_to_play_title), stringResource(R.string.how_to_play_description))
            Spacer(Modifier.padding(8.dp))
            AboutCard(stringResource(R.string.features_title), stringResource(R.string.features_description))
        }
    }
}

@Composable
private fun AboutCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Spacer(Modifier.padding(4.dp))
            Text(body, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
