package ir.sharif.xo.ads

import android.app.Activity
import kotlinx.coroutines.CancellationException

class TapsellStrategy(
    private val gateway: AdNetworkGateway,
    private val observer: AdObserver = AdObserver.NONE
) : AdStrategy {
    override suspend fun loadAndShowAd(activity: Activity, request: AdRequest): AdResult =
        gateway.safeLoadAndShow(
            activity = activity,
            placementId = request.tapsellPlacementId,
            type = request.type,
            providerName = "Tapsell",
            observer = observer
        )

    override fun close() = gateway.close()
}

class AdiveryStrategy(
    private val gateway: AdNetworkGateway,
    private val observer: AdObserver = AdObserver.NONE
) : AdStrategy {
    override suspend fun loadAndShowAd(activity: Activity, request: AdRequest): AdResult =
        gateway.safeLoadAndShow(
            activity = activity,
            placementId = request.adiveryPlacementId,
            type = request.type,
            providerName = "Adivery",
            observer = observer
        )

    override fun close() = gateway.close()
}

class MixedAdStrategy(
    private val tapsell: AdStrategy,
    private val adivery: AdStrategy,
    private val observer: AdObserver = AdObserver.NONE
) : AdStrategy {
    override suspend fun loadAndShowAd(activity: Activity, request: AdRequest): AdResult {
        val primary = runCatching { tapsell.loadAndShowAd(activity, request) }
            .getOrElse { throwable ->
                if (throwable is CancellationException) throw throwable
                AdResult.Failed(FailureReason.UNKNOWN)
            }
        if (primary is AdResult.Shown) return primary

        observer.breadcrumb("Tapsell unavailable; falling back to Adivery")
        return runCatching { adivery.loadAndShowAd(activity, request) }
            .getOrElse { throwable ->
                if (throwable is CancellationException) throw throwable
                AdResult.Failed(FailureReason.UNKNOWN)
            }
    }

    override fun close() {
        tapsell.close()
        adivery.close()
    }
}

interface AdObserver {
    fun breadcrumb(message: String)

    companion object {
        val NONE: AdObserver = object : AdObserver {
            override fun breadcrumb(message: String) = Unit
        }
    }
}

private suspend fun AdNetworkGateway.safeLoadAndShow(
    activity: Activity,
    placementId: String,
    type: AdType,
    providerName: String,
    observer: AdObserver
): AdResult {
    if (placementId.isBlank()) return AdResult.Failed(FailureReason.NOT_CONFIGURED)
    observer.breadcrumb("$providerName ${type.name.lowercase()} requested")
    return try {
        loadAndShowAd(activity, placementId, type).also { result ->
            if (result is AdResult.Failed && result.reason != FailureReason.NO_FILL && result.reason != FailureReason.NETWORK) {
                observer.breadcrumb("$providerName failed: ${result.reason}")
            }
        }
    } catch (throwable: Throwable) {
        if (throwable is CancellationException) throw throwable
        observer.breadcrumb("$providerName unavailable")
        AdResult.Failed(FailureReason.UNKNOWN)
    }
}
