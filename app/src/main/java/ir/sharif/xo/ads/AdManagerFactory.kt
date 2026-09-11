package ir.sharif.xo.ads

import android.app.Activity
import android.content.Context
import ir.sharif.xo.BuildConfig

object AdManagerFactory {
    fun create(
        context: Context,
        activityProvider: () -> Activity?
    ): AdManager {
        val networkConfig = AdNetworkConfig(
            provider = AdProvider.parse(BuildConfig.AD_PROVIDER),
            tapsellAppId = BuildConfig.TAPSELL_APP_ID,
            adiveryAppId = BuildConfig.ADIVERY_APP_ID,
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
