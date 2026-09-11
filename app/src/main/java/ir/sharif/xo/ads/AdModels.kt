package ir.sharif.xo.ads

import android.app.Activity
import android.view.ViewGroup

/** Provider choice delivered by remote configuration. */
enum class AdProvider(val wireValue: String) {
    TAPSELL("Tapsell"),
    ADIVERY("Adivery"),
    MIXED("mixed");

    companion object {
        fun parse(value: String?): AdProvider {
            val normalized = value?.trim() ?: return MIXED
            return entries.firstOrNull {
                it.wireValue.equals(normalized, ignoreCase = true) ||
                    it.name.equals(normalized, ignoreCase = true)
            } ?: MIXED
        }
    }
}

enum class AdType {
    BANNER,
    INTERSTITIAL,
    NATIVE,
    REWARDED
}

/** Credentials and placements are supplied by build configuration, never by UI code. */
data class AdNetworkConfig(
    val provider: AdProvider = AdProvider.MIXED,
    val tapsellAppId: String = "",
    val adiveryAppId: String = "",
    val remoteConfigUrl: String = ""
)

data class AdRequest(
    val zoneId: String,
    val tapsellPlacementId: String,
    val adiveryPlacementId: String,
    val type: AdType = AdType.REWARDED,
    /** Host view for native ads. Required by [AdType.NATIVE], ignored by other types. */
    val nativeContainer: ViewGroup? = null
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
    fun showAd(zoneId: String, type: AdType = AdType.REWARDED)
}

/** A strategy owns one provider or a provider sequence. */
interface AdStrategy {
    suspend fun loadAndShowAd(activity: Activity, request: AdRequest): AdResult

    /** Tears down a native ad previously rendered into [container]. No-op when unused. */
    fun releaseNativeAd(container: ViewGroup) = Unit

    fun close()
}

/** SDK bridge keeps vendor APIs isolated from mediation policy. */
interface AdNetworkGateway {
    suspend fun loadAndShowAd(
        activity: Activity,
        placementId: String,
        type: AdType,
        container: ViewGroup? = null
    ): AdResult

    /** Tears down a native ad previously rendered into [container]. No-op when unused. */
    fun releaseNativeAd(container: ViewGroup) = Unit

    fun close()
}
