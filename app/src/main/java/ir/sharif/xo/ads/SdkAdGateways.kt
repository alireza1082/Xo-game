package ir.sharif.xo.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.ViewGroup
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryNativeAdView
import ir.tapsell.plus.AdHolder
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.NativeManager
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

/** Native loads get the same budget as Adivery's interstitial/rewarded polling. */
private const val NATIVE_TIMEOUT_MS = 10_000L

/** Tapsell Plus gateway. */
class TapsellAdGateway(
    private val appId: String
) : AdNetworkGateway {
    private val initializationMutex = Mutex()
    private var initialized = false

    override suspend fun loadAndShowAd(
        activity: Activity,
        placementId: String,
        type: AdType,
        container: ViewGroup?
    ): AdResult {
        if (placementId.isBlank() || appId.isBlank()) return AdResult.Failed(FailureReason.NOT_CONFIGURED)
        return try {
            if (!ensureInitialized(activity)) return AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
            when (type) {
                AdType.INTERSTITIAL -> requestInterstitial(activity, placementId)
                AdType.REWARDED -> requestRewarded(activity, placementId)
                AdType.BANNER -> AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
                // Tapsell renders native ads into a template whose root view must be a Google
                // Mobile Ads NativeAdView, so the format cannot work without play-services-ads.
                AdType.NATIVE -> when {
                    container == null -> AdResult.Failed(FailureReason.INVALID_CONTEXT)
                    !hasGoogleMobileAds() -> AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
                    else -> requestNative(activity, placementId, container)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
        }
    }

    private fun hasGoogleMobileAds(): Boolean =
        runCatching { Class.forName("com.google.android.gms.ads.MobileAds") }.isSuccess

    override fun releaseNativeAd(container: ViewGroup) {
        val responseId = container.tag as? String
        val activity = container.context as? Activity
        container.tag = null
        container.removeAllViews()
        if (activity != null && !responseId.isNullOrBlank()) {
            runCatching { TapsellPlus.destroyNativeBanner(activity, responseId) }
        }
    }

    override fun close() = Unit

    private suspend fun requestRewarded(activity: Activity, placementId: String): AdResult =
        requestVideo(activity, placementId, rewarded = true)

    private suspend fun requestInterstitial(activity: Activity, placementId: String): AdResult =
        requestVideo(activity, placementId, rewarded = false)

    private suspend fun requestVideo(
        activity: Activity,
        placementId: String,
        rewarded: Boolean
    ): AdResult = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            val callback = object : AdRequestCallback() {
                override fun response(ad: TapsellPlusAdModel) {
                    if (!continuation.isActive) return
                    val responseId = ad.responseId
                    if (responseId.isNullOrBlank()) {
                        continuation.resume(AdResult.Failed(FailureReason.UNKNOWN))
                        return
                    }
                    val listener = object : AdShowListener() {
                        override fun onClosed(adModel: TapsellPlusAdModel) {
                            if (continuation.isActive) continuation.resume(AdResult.Shown)
                        }

                        override fun onError(error: TapsellPlusErrorModel) {
                            if (continuation.isActive) continuation.resume(AdResult.Failed(FailureReason.UNKNOWN))
                        }
                    }
                    if (rewarded) {
                        TapsellPlus.showRewardedVideoAd(activity, responseId, listener)
                    } else {
                        TapsellPlus.showInterstitialAd(activity, responseId, listener)
                    }
                }

                override fun error(message: String) {
                    if (continuation.isActive) continuation.resume(classifyFailure(message))
                }
            }
            if (rewarded) {
                TapsellPlus.requestRewardedVideoAd(activity, placementId, callback)
            } else {
                TapsellPlus.requestInterstitialAd(activity, placementId, callback)
            }
        }
    }

    private suspend fun requestNative(
        activity: Activity,
        placementId: String,
        container: ViewGroup
    ): AdResult = withContext(Dispatchers.Main.immediate) {
        container.tag = null
        container.removeAllViews()
        withTimeoutOrNull(NATIVE_TIMEOUT_MS.milliseconds) {
            suspendCancellableCoroutine<AdResult> { continuation ->
                TapsellPlus.requestNativeAd(activity, placementId, object : AdRequestCallback() {
                    override fun response(ad: TapsellPlusAdModel) {
                        if (!continuation.isActive) return
                        val responseId = ad.responseId
                        if (responseId.isNullOrBlank()) {
                            continuation.resume(AdResult.Failed(FailureReason.UNKNOWN))
                            return
                        }
                        // SDK-sanctioned template; its Google Mobile Ads root is why this path is
                        // gated behind hasGoogleMobileAds() above.
                        val holder = try {
                            AdHolder(
                                NativeManager.Builder()
                                    .setParentView(container)
                                    .setContentViewTemplate(ir.tapsell.plus.R.layout.native_banner)
                                    .inflateTemplate(activity)
                            )
                        } catch (_: Throwable) {
                            continuation.resume(AdResult.Failed(FailureReason.UNKNOWN))
                            return
                        }
                        container.tag = responseId
                        TapsellPlus.showNativeAd(
                            activity,
                            responseId,
                            holder,
                            object : AdShowListener() {
                                override fun onOpened(adModel: TapsellPlusAdModel) {
                                    if (continuation.isActive) continuation.resume(AdResult.Shown)
                                }

                                override fun onClosed(adModel: TapsellPlusAdModel) {
                                    if (continuation.isActive) continuation.resume(AdResult.Shown)
                                }

                                override fun onError(error: TapsellPlusErrorModel) {
                                    if (continuation.isActive) {
                                        continuation.resume(AdResult.Failed(FailureReason.UNKNOWN))
                                    }
                                }
                            }
                        )
                    }

                    override fun error(message: String) {
                        if (continuation.isActive) continuation.resume(classifyFailure(message))
                    }
                })
            }
        } ?: AdResult.Failed(FailureReason.NO_FILL)
    }

    private suspend fun ensureInitialized(activity: Activity): Boolean = initializationMutex.withLock {
        if (initialized) return@withLock true
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                TapsellPlus.initialize(activity, appId, object : ir.tapsell.plus.TapsellPlusInitListener {
                    override fun onInitializeSuccess(adNetworks: ir.tapsell.plus.model.AdNetworks) {
                        initialized = true
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onInitializeFailed(
                        adNetwork: ir.tapsell.plus.model.AdNetworks,
                        error: ir.tapsell.plus.model.AdNetworkError
                    ) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                })
            }
        }
    }
}

private fun classifyFailure(message: String): AdResult.Failed {
    val normalized = message.lowercase()
    return AdResult.Failed(
        when {
            "fill" in normalized || "available" in normalized -> FailureReason.NO_FILL
            "network" in normalized || "timeout" in normalized -> FailureReason.NETWORK
            else -> FailureReason.UNKNOWN
        }
    )
}

/** Adivery gateway. */
class AdiveryAdGateway(
    application: Context,
    private val appId: String,
    private val timeoutMillis: Long = 10_000L,
    private val pollMillis: Long = 250L
) : AdNetworkGateway {
    private val appContext = application.applicationContext
    private val app = appContext as? Application
    private var configured = false

    init {
        if (appId.isNotBlank()) {
            configured = runCatching {
                app?.let { Adivery.configure(it, appId) } != null
            }.getOrDefault(false)
        }
    }

    override suspend fun loadAndShowAd(
        activity: Activity,
        placementId: String,
        type: AdType,
        container: ViewGroup?
    ): AdResult {
        if (placementId.isBlank() || !configured) return AdResult.Failed(FailureReason.NOT_CONFIGURED)
        return try {
            when (type) {
                AdType.INTERSTITIAL -> showInterstitial(placementId)
                AdType.REWARDED -> showRewarded(placementId)
                AdType.BANNER -> AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
                AdType.NATIVE -> if (container == null) {
                    AdResult.Failed(FailureReason.INVALID_CONTEXT)
                } else {
                    showNative(placementId, container)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
        }
    }

    override fun close() = Unit

    private suspend fun showInterstitial(placementId: String): AdResult {
        withContext(Dispatchers.Main.immediate) { Adivery.prepareInterstitialAd(appContext, placementId) }
        return awaitLoadedAndShow(placementId)
    }

    private suspend fun showRewarded(placementId: String): AdResult {
        withContext(Dispatchers.Main.immediate) { Adivery.prepareRewardedAd(appContext, placementId) }
        return awaitLoadedAndShow(placementId)
    }

    private suspend fun showNative(placementId: String, container: ViewGroup): AdResult =
        withContext(Dispatchers.Main.immediate) {
            withTimeoutOrNull(timeoutMillis.milliseconds) {
                suspendCancellableCoroutine<AdResult> { continuation ->
                    container.removeAllViews()
                    val nativeAdView = AdiveryNativeAdView(container.context).apply {
                        setPlacementId(placementId)
                        setNativeAdLayout(ir.sharif.xo.R.layout.adivery_native_ad)
                        setListener(object : AdiveryAdListener() {
                            override fun onAdLoaded() {
                                if (continuation.isActive) continuation.resume(AdResult.Shown)
                            }

                            override fun onError(reason: String) {
                                if (continuation.isActive) continuation.resume(classifyFailure(reason))
                            }
                        })
                    }
                    container.addView(
                        nativeAdView,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                    nativeAdView.loadAd()
                }
            } ?: AdResult.Failed(FailureReason.NO_FILL)
        }

    private suspend fun awaitLoadedAndShow(placementId: String): AdResult {
        var elapsed = 0L
        while (elapsed < timeoutMillis) {
            val loaded = withContext(Dispatchers.Main.immediate) { Adivery.isLoaded(placementId) }
            if (loaded) {
                withContext(Dispatchers.Main.immediate) { Adivery.showAd(placementId) }
                return AdResult.Shown
            }
            delay(pollMillis.milliseconds)
            elapsed += pollMillis
        }
        return AdResult.Failed(FailureReason.NO_FILL)
    }
}
