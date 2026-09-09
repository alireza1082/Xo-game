package com.example.android.xo.ads

import android.app.Activity

/**
 * Safe default gateway used when provider SDK credentials are not configured.
 * Replace each implementation with the corresponding SDK adapter once production placement IDs
 * are supplied. Keeping this seam now lets mediation policy and tests remain provider-independent.
 */
class UnconfiguredAdGateway : AdNetworkGateway {
    override suspend fun loadAndShowAd(activity: Activity, placementId: String): AdResult =
        if (placementId.isBlank()) {
            AdResult.Failed(FailureReason.NOT_CONFIGURED)
        } else {
            AdResult.Failed(FailureReason.SDK_UNAVAILABLE)
        }

    override fun close() = Unit
}
