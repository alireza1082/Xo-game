package ir.sharif.xo.ads

import android.app.Activity
import ir.sharif.xo.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Resolves the current strategy from local configuration and owns its lifecycle.
 * Calls return immediately; SDK work is dispatched off the UI thread and SDK callbacks are
 * marshalled back by the provider gateways when required.
 */
class AdManager(
    private val configRepository: AdConfigRepository,
    private val networkConfig: AdNetworkConfig,
    private val tapsellStrategy: AdStrategy,
    private val adiveryStrategy: AdStrategy,
    private val observer: AdObserver = AdObserver.NONE,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val activityProvider: (() -> Activity?)? = null
) : Ads {
    val provider: StateFlow<AdProvider> = configRepository.provider.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AdProvider.parse(BuildConfig.AD_PROVIDER)
    )

    private val lock = Any()
    private var closed = false

    init {
        scope.launch {
            runCatching { configRepository.refreshFromRemote() }
                .onSuccess { provider ->
                    if (provider != null) observer.breadcrumb("Remote ad config saved: ${provider.wireValue.uppercase()}")
                }
        }
    }

    override fun showAd(zoneId: String, type: AdType) {
        val activity = activityProvider?.invoke() ?: return
        showAd(activity, zoneId, type)
    }

    fun showAd(
        activity: Activity,
        zoneId: String,
        type: AdType = AdType.REWARDED,
        onResult: ((AdResult) -> Unit)? = null
    ) {
        if (zoneId.isBlank() || activity.isFinishing || activity.isDestroyed) {
            onResult?.invoke(AdResult.Failed(FailureReason.INVALID_CONTEXT))
            return
        }
        synchronized(lock) {
            if (closed) {
                onResult?.invoke(AdResult.Failed(FailureReason.SDK_UNAVAILABLE))
                return
            }
        }
        val request = AdRequest(
            zoneId = zoneId,
            tapsellPlacementId = AdPlacements.tapsell(type),
            adiveryPlacementId = AdPlacements.adivery(type),
            type = type
        )
        scope.launch {
            val provider = configRepository.currentProvider()
            ensureActive()
            observer.breadcrumb("Config loaded: ${provider.wireValue.uppercase()}")
            val result = withContext(Dispatchers.Default) {
                if (!isConfigured(provider, type)) AdResult.Failed(FailureReason.NOT_CONFIGURED)
                else strategyFor(provider).loadAndShowAd(activity, request)
            }
            ensureActive()
            onResult?.invoke(result)
        }
    }

    fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
        }
        tapsellStrategy.close()
        adiveryStrategy.close()
        scope.cancel()
    }

    private fun strategyFor(provider: AdProvider): AdStrategy = when (provider) {
        AdProvider.TAPSELL -> tapsellStrategy
        AdProvider.ADIVERY -> adiveryStrategy
        AdProvider.MIXED -> MixedAdStrategy(tapsellStrategy, adiveryStrategy, observer)
    }

    private fun isConfigured(provider: AdProvider, type: AdType): Boolean = when (provider) {
        AdProvider.TAPSELL -> networkConfig.tapsellAppId.isNotBlank() && AdPlacements.tapsell(type).isNotBlank()
        AdProvider.ADIVERY -> networkConfig.adiveryAppId.isNotBlank() && AdPlacements.adivery(type).isNotBlank()
        AdProvider.MIXED -> (networkConfig.tapsellAppId.isNotBlank() && AdPlacements.tapsell(type).isNotBlank()) ||
            (networkConfig.adiveryAppId.isNotBlank() && AdPlacements.adivery(type).isNotBlank())
    }
}
