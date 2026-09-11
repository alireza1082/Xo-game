package com.example.android.xo.ads

import android.app.Activity

/** Safe default gateway used when provider SDK credentials are not configured. */
class UnconfiguredAdGateway : AdNetworkGateway {
    override suspend fun loadAndShowAd(
        activity: Activity,
        placementId: String,
        type: AdType
    ): AdResult = AdResult.Failed(
        if (placementId.isBlank()) FailureReason.NOT_CONFIGURED else FailureReason.SDK_UNAVAILABLE
    )

    override fun close() = Unit
}
