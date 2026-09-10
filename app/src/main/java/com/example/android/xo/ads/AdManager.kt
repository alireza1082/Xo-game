package com.example.android.xo.ads

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive

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

    override fun showAd(zoneId: String) {
        val activity = activityProvider?.invoke() ?: return
        showAd(activity, zoneId)
    }

    fun showAd(activity: Activity, zoneId: String, onResult: ((AdResult) -> Unit)? = null) {
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
            tapsellPlacementId = networkConfig.tapsellRewardedPlacementId,
            adiveryPlacementId = networkConfig.adiveryRewardedPlacementId
        )
        scope.launch {
            val provider = configRepository.currentProvider()
            ensureActive()
            observer.breadcrumb("Config loaded: ${provider.wireValue.uppercase()}")
            val result = withContext(Dispatchers.Default) {
                if (!isConfigured(provider)) AdResult.Failed(FailureReason.NOT_CONFIGURED)
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

    private fun isConfigured(provider: AdProvider): Boolean = when (provider) {
        AdProvider.TAPSELL -> networkConfig.hasTapsellPlacement()
        AdProvider.ADIVERY -> networkConfig.hasAdiveryPlacement()
        AdProvider.MIXED -> networkConfig.hasTapsellPlacement() || networkConfig.hasAdiveryPlacement()
    }
}
