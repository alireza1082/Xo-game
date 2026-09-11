package com.example.android.xo.ads

import android.app.Activity
import android.content.Context
import com.example.android.xo.BuildConfig

object AdManagerFactory {
    fun create(
        context: Context,
        activityProvider: () -> Activity?
    ): AdManager {
        val networkConfig = AdNetworkConfig(
            provider = AdProvider.parse(BuildConfig.AD_PROVIDER),
            tapsellAppId = BuildConfig.TAPSELL_APP_ID,
            tapsellBannerPlacementId = BuildConfig.TAPSELL_BANNER_PLACEMENT,
            tapsellInterstitialPlacementId = BuildConfig.TAPSELL_INTERSTITIAL_PLACEMENT,
            tapsellNativePlacementId = BuildConfig.TAPSELL_NATIVE_PLACEMENT,
            tapsellRewardedPlacementId = BuildConfig.TAPSELL_REWARDED_PLACEMENT,
            adiveryAppId = BuildConfig.ADIVERY_APP_ID,
            adiveryBannerPlacementId = BuildConfig.ADIVERY_BANNER_PLACEMENT,
            adiveryInterstitialPlacementId = BuildConfig.ADIVERY_INTERSTITIAL_PLACEMENT,
            adiveryNativePlacementId = BuildConfig.ADIVERY_NATIVE_PLACEMENT,
            adiveryRewardedPlacementId = BuildConfig.ADIVERY_REWARDED_PLACEMENT,
            remoteConfigUrl = BuildConfig.AD_REMOTE_CONFIG_URL
        )
        val observer = SentryAdMonitoring.observer()
        val repository = DataStoreAdConfigRepository(
            context = context,
            remoteConfigUrl = networkConfig.remoteConfigUrl
        )
        val tapsell = TapsellStrategy(TapsellAdGateway(networkConfig.tapsellAppId), observer)
        val adivery = AdiveryStrategy(
            AdiveryAdGateway(context, networkConfig.adiveryAppId),
            observer
        )
        return AdManager(
            configRepository = repository,
            networkConfig = networkConfig,
            tapsellStrategy = tapsell,
            adiveryStrategy = adivery,
            observer = observer,
            activityProvider = activityProvider
        )
    }
}
