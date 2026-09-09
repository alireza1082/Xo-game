package com.example.android.xo.ads

import android.app.Activity

/** Provider choice delivered by remote configuration. */
enum class AdProvider(val wireValue: String) {
    TAPSELL("tapsell"),
    ADIVERY("adivery"),
    MIXED("mixed");

    companion object {
        fun parse(value: String?): AdProvider = entries.firstOrNull {
            it.wireValue == value?.trim()?.lowercase()
        } ?: MIXED
    }
}

/** Credentials and placements are supplied by build configuration, never by UI code. */
data class AdNetworkConfig(
    val provider: AdProvider = AdProvider.MIXED,
    val tapsellAppId: String = "",
    val tapsellRewardedPlacementId: String = "",
    val adiveryAppId: String = "",
    val adiveryRewardedPlacementId: String = "",
    val remoteConfigUrl: String = ""
) {
    fun hasTapsellPlacement() = tapsellAppId.isNotBlank() && tapsellRewardedPlacementId.isNotBlank()
    fun hasAdiveryPlacement() = adiveryAppId.isNotBlank() && adiveryRewardedPlacementId.isNotBlank()
}

data class AdRequest(
    val zoneId: String,
    val tapsellPlacementId: String,
    val adiveryPlacementId: String
)

sealed interface AdResult {
    data object Shown : AdResult
    data object Unavailable : AdResult
    data class Failed(val reason: FailureReason) : AdResult
}

enum class FailureReason {
    NOT_CONFIGURED,
    NO_FILL,
    NETWORK,
    SDK_UNAVAILABLE,
    INVALID_CONTEXT,
    UNKNOWN
}

/** Provider-neutral entry point used by the application. */
interface Ads {
    fun showAd(zoneId: String)
}

/** A strategy owns one provider or a provider sequence. */
interface AdStrategy {
    suspend fun loadAndShowAd(activity: Activity, request: AdRequest): AdResult
    fun close()
}

/** SDK bridge keeps vendor APIs isolated from mediation policy. */
interface AdNetworkGateway {
    suspend fun loadAndShowAd(activity: Activity, placementId: String): AdResult
    fun close()
}
