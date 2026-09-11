package ir.sharif.xo.ui

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ir.sharif.xo.BuildConfig
import ir.sharif.xo.R
import ir.sharif.xo.XoApplication
import ir.sharif.xo.ads.AdProvider
import ir.sharif.xo.ads.AdResult
import ir.sharif.xo.ads.AdType
import ir.sharif.xo.ads.BannerAdView
import ir.sharif.xo.ads.FailureReason
import ir.sharif.xo.data.GamePreferences
import ir.sharif.xo.engine.GameMode
import ir.sharif.xo.engine.Player

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
    var showAdTester by remember { mutableStateOf(false) }
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
        Spacer(Modifier.height(24.dp))
        // Debug-only entry point for the ad tester. Remove together with AdTesterDialog before release.
        OutlinedButton(
            onClick = { showAdTester = true },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Debug: Test Ads") }
    }

    if (showAdTester) {
        AdTesterDialog(onDismiss = { showAdTester = false })
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.dialog_ai_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.select_difficulty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DifficultyOption(
                        label = stringResource(R.string.difficulty_easy),
                        selected = difficulty == GameMode.VS_AI_EASY,
                        onClick = { difficulty = GameMode.VS_AI_EASY },
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyOption(
                        label = stringResource(R.string.difficulty_medium),
                        selected = difficulty == GameMode.VS_AI_MEDIUM,
                        onClick = { difficulty = GameMode.VS_AI_MEDIUM },
                        modifier = Modifier.weight(1f)
                    )
                    DifficultyOption(
                        label = stringResource(R.string.difficulty_impossible),
                        selected = difficulty == GameMode.VS_AI_IMPOSSIBLE,
                        onClick = { difficulty = GameMode.VS_AI_IMPOSSIBLE },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.select_symbol),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SymbolOption(
                        player = Player.X,
                        description = stringResource(R.string.symbol_x_desc),
                        selected = symbol == Player.X,
                        onClick = { symbol = Player.X },
                        modifier = Modifier.weight(1f)
                    )
                    SymbolOption(
                        player = Player.O,
                        description = stringResource(R.string.symbol_o_desc),
                        selected = symbol == Player.O,
                        onClick = { symbol = Player.O },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
                Button(
                    onClick = { onSaveSymbol(symbol); onStart(difficulty, symbol) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(R.string.start_game))
                }
            }
        }
    }
}

@Composable
private fun DifficultyOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(180),
        label = "difficultyBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "difficultyContent"
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            },
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = contentColor,
        enabled = true,
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = if (selected) backgroundColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SymbolOption(
    player: Player,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (player == Player.X) {
        if (isSystemInDarkTheme()) ir.sharif.xo.ui.theme.XoGameColors.xOnBoard
        else ir.sharif.xo.ui.theme.XoGameColors.x
    } else {
        if (isSystemInDarkTheme()) ir.sharif.xo.ui.theme.XoGameColors.oOnBoard
        else ir.sharif.xo.ui.theme.XoGameColors.o
    }
    val borderColor by animateColorAsState(
        targetValue = if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(220),
        label = "symbolBorder"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 1.dp,
        animationSpec = tween(220),
        label = "symbolBorderWidth"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = tween(220),
        label = "symbolScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 1.dp,
        animationSpec = tween(220),
        label = "symbolElevation"
    )
    val shape = MaterialTheme.shapes.medium

    Surface(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 0.72f else 0.45f),
        enabled = true,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = elevation,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = player.name,
                color = accent,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Temporary ad verification surface. Exercises all ad types against the selected provider and
 * reports the exact [AdResult] so failures can be diagnosed. Delete this and its entry button in
 * [HomeScreen] once provider behaviour has been confirmed.
 */
@Composable
private fun AdTesterDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val ads = remember(context) { (context.applicationContext as? XoApplication)?.ads }
    // Shared host for whichever provider renders a native ad; always attached while the dialog is up.
    val nativeContainer = remember(context) { FrameLayout(context) }
    val scope = rememberCoroutineScope()
    val providerState = ads?.provider?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(AdProvider.parse(BuildConfig.AD_PROVIDER)) }
    val provider by providerState
    var nativeAdShown by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf("Idle") }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun runAd(
        type: AdType,
        label: String,
        container: ViewGroup? = null,
        onShown: () -> Unit = {}
    ) {
        val manager = ads
        if (manager == null || activity == null) {
            val message = "$label: Failed: ${FailureReason.INVALID_CONTEXT}"
            lastResult = message
            toast(message)
            return
        }
        lastResult = "$label: loading..."
        manager.showAd(activity, "test_${type.name.lowercase()}", type, container) { result ->
            val message = "$label: ${result.describe()}"
            lastResult = message
            toast(message)
            if (result is AdResult.Shown) onShown()
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ad Tester",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Debug only - not for release",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Provider",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProviderOption(
                        label = "Tapsell",
                        selected = provider == AdProvider.TAPSELL,
                        enabled = ads != null,
                        onClick = {
                            nativeAdShown = false
                            ads?.releaseNativeAd(nativeContainer)
                            scope.launch {
                                runCatching { ads?.selectProvider(AdProvider.TAPSELL) }
                                    .onFailure { toast("Provider switch failed: ${it.message}") }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ProviderOption(
                        label = "Adivery",
                        selected = provider == AdProvider.ADIVERY,
                        enabled = ads != null,
                        onClick = {
                            nativeAdShown = false
                            ads?.releaseNativeAd(nativeContainer)
                            scope.launch {
                                runCatching { ads?.selectProvider(AdProvider.ADIVERY) }
                                    .onFailure { toast("Provider switch failed: ${it.message}") }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Active: ${provider.wireValue.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { runAd(AdType.INTERSTITIAL, "Interstitial") },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Show Interstitial Ad") }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { runAd(AdType.REWARDED, "Rewarded") },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Show Rewarded Ad") }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        nativeAdShown = false
                        runAd(AdType.NATIVE, "Native", nativeContainer) { nativeAdShown = true }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Load Native Ad") }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!nativeAdShown) DummyNativeAd()
                    AndroidView(
                        factory = { nativeContainer },
                        onRelease = { container -> ads?.releaseNativeAd(container) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = lastResult,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BannerAdView(
                        modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp).height(50.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun ProviderOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(180),
        label = "providerBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "providerContent"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = contentColor,
        enabled = enabled,
        border = BorderStroke(
            width = if (selected) 0.dp else 1.dp,
            color = if (selected) backgroundColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

/** Placeholder shown in place of a real native ad once a native load reports success. */
@Composable
private fun DummyNativeAd() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = "Sponsored",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Native ad placeholder",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Replace with the real native ad layout when wiring the SDK template.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun AdResult.describe(): String = when (this) {
    AdResult.Shown -> "Shown"
    AdResult.Unavailable -> "Unavailable"
    is AdResult.Failed -> "Failed: ${reason.name}"
}
