package com.example.android.xo.ads

import android.app.Activity
import android.content.Context
import android.app.Application
import com.adivery.sdk.Adivery
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** Tapsell Plus rewarded implementation. SDK calls are isolated from mediation policy. */
class TapsellAdGateway(
    private val context: Context,
    private val appId: String
) : AdNetworkGateway {
    private val initializationMutex = Mutex()
    private var initialized = false

    override suspend fun loadAndShowAd(activity: Activity, placementId: String): AdResult {
        if (placementId.isBlank() || appId.isBlank()) return AdResult.Failed(FailureReason.NOT_CONFIGURED)
        return try {
            if (!ensureInitialized(activity)) return AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
            withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine { continuation ->
                    TapsellPlus.requestRewardedVideoAd(
                        activity,
                        placementId,
                        object : AdRequestCallback() {
                            override fun response(ad: TapsellPlusAdModel) {
                                if (!continuation.isActive) return
                                val responseId = ad.responseId
                                if (responseId.isNullOrBlank()) {
                                    continuation.resume(AdResult.Failed(FailureReason.UNKNOWN))
                                    return
                                }
                                TapsellPlus.showRewardedVideoAd(
                                    activity,
                                    responseId,
                                    object : AdShowListener() {
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
                                if (continuation.isActive) continuation.resume(classify(message))
                            }
                        }
                    )
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
        }
    }

    override fun close() = Unit

    private suspend fun ensureInitialized(activity: Activity): Boolean = initializationMutex.withLock {
        if (initialized) return@withLock true
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                TapsellPlus.initialize(activity, appId, object : TapsellPlusInitListener {
                    override fun onInitializeSuccess(adNetworks: AdNetworks) {
                        initialized = true
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onInitializeFailed(adNetwork: AdNetworks, error: AdNetworkError) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                })
            }
        }
    }

    private fun classify(message: String): AdResult.Failed {
        val normalized = message.lowercase()
        return AdResult.Failed(
            when {
                "fill" in normalized || "available" in normalized -> FailureReason.NO_FILL
                "network" in normalized || "timeout" in normalized -> FailureReason.NETWORK
                else -> FailureReason.UNKNOWN
            }
        )
    }
}

/** Adivery rewarded implementation. Adivery exposes readiness synchronously, so polling uses delay. */
class AdiveryAdGateway(
    application: Context,
    appId: String,
    private val timeoutMillis: Long = 10_000L,
    private val pollMillis: Long = 250L
) : AdNetworkGateway {
    private val appContext = application.applicationContext
    private val app = appContext as? Application

    init {
        if (appId.isNotBlank()) {
            runCatching { app?.let { Adivery.configure(it, appId) } }
        }
    }

    override suspend fun loadAndShowAd(activity: Activity, placementId: String): AdResult {
        if (placementId.isBlank()) return AdResult.Failed(FailureReason.NOT_CONFIGURED)
        return try {
            withContext(Dispatchers.Main.immediate) {
                Adivery.prepareRewardedAd(appContext, placementId)
            }
            var elapsed = 0L
            while (elapsed < timeoutMillis) {
                val loaded = withContext(Dispatchers.Main.immediate) {
                    Adivery.isLoaded(placementId)
                }
                if (loaded) {
                    withContext(Dispatchers.Main.immediate) { Adivery.showAd(placementId) }
                    return AdResult.Shown
                }
                delay(pollMillis)
                elapsed += pollMillis
            }
            AdResult.Failed(FailureReason.NO_FILL)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
        }
    }

    override fun close() = Unit
}
