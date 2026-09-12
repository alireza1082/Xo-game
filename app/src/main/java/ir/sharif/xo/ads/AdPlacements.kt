package ir.sharif.xo.ads

object AdPlacements {
    const val TAPSELL_BANNER = "634074967c7871496f5e299a"
    const val TAPSELL_INTERSTITIAL = "6340763d86e87557c99d3dff"
    const val TAPSELL_NATIVE = "your_tapsell_native_id"
    const val TAPSELL_REWARDED = "6aa3d0cd796a202335abbbc9"

    const val ADIVERY_BANNER = "49e372c4-8e72-4e60-97b5-9d99c6250959"
    const val ADIVERY_INTERSTITIAL = "ebedbba6-fb48-4b23-bfd1-cdf1701bbfa6"
    const val ADIVERY_NATIVE = "e2912e75-32e2-43ed-af6e-adf288815e78"
    const val ADIVERY_REWARDED = "4c8019f9-8faf-4aee-8ecf-4b21e21080c2"

    fun tapsell(type: AdType): String = when (type) {
        AdType.BANNER -> TAPSELL_BANNER
        AdType.INTERSTITIAL -> TAPSELL_INTERSTITIAL
        AdType.NATIVE -> TAPSELL_NATIVE
        AdType.REWARDED -> TAPSELL_REWARDED
    }

    fun adivery(type: AdType): String = when (type) {
        AdType.BANNER -> ADIVERY_BANNER
        AdType.INTERSTITIAL -> ADIVERY_INTERSTITIAL
        AdType.NATIVE -> ADIVERY_NATIVE
        AdType.REWARDED -> ADIVERY_REWARDED
    }

    fun isConfigured(placementId: String): Boolean =
        placementId.isNotBlank() && !placementId.startsWith("your_", ignoreCase = true)

    fun isConfigured(type: AdType, provider: AdProvider): Boolean = when (provider) {
        AdProvider.TAPSELL -> isConfigured(tapsell(type))
        AdProvider.ADIVERY -> isConfigured(adivery(type))
        AdProvider.MIXED -> isConfigured(tapsell(type)) || isConfigured(adivery(type))
    }
}
