package ir.sharif.xo.ads

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isNotEmpty
import com.adivery.sdk.AdiveryBannerAdView
import com.adivery.sdk.BannerSize
import ir.sharif.xo.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adManager: AdManager? = (LocalContext.current.applicationContext as? ir.sharif.xo.XoApplication)?.ads
) {
    val context = LocalContext.current
    val provider by adManager?.provider?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(AdProvider.parse(BuildConfig.AD_PROVIDER)) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext -> FrameLayout(viewContext) },
        update = { container ->
            if (container.isNotEmpty()) return@AndroidView
            when (provider) {
                AdProvider.ADIVERY -> loadAdiveryBanner(container, context)
                AdProvider.TAPSELL -> loadTapsellBanner(container)
                AdProvider.MIXED -> loadAdiveryBanner(container, context)
            }
        },
        onRelease = { container ->
            val activity = container.context as? Activity
            val responseId = container.tag as? String
            if (activity != null && !responseId.isNullOrBlank()) {
                runCatching { TapsellPlus.destroyStandardBanner(activity, responseId, container) }
            }
            container.removeAllViews()
        }
    )
}

private fun loadAdiveryBanner(container: FrameLayout, context: Context) {
    val placementId = AdPlacements.ADIVERY_BANNER
    if (placementId.isBlank()) return
    runCatching {
        val banner = AdiveryBannerAdView(context).apply {
            setBannerSize(BannerSize.BANNER)
            setPlacementId(placementId)
            setBannerAdListener(object : com.adivery.sdk.AdiveryAdListener() {
                override fun onAdLoaded() = Unit
                override fun onError(reason: String) = container.removeAllViews()
                override fun onAdClicked() = Unit
            })
        }
        container.addView(
            banner,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
        banner.loadAd()
    }
}

private fun loadTapsellBanner(container: FrameLayout) {
    val placementId = AdPlacements.TAPSELL_BANNER
    if (placementId.isBlank()) return
    TapsellPlus.requestStandardBannerAd(
        container.context as? Activity ?: return,
        placementId,
        TapsellPlusBannerType.BANNER_320x50,
        object : AdRequestCallback() {
            override fun response(ad: TapsellPlusAdModel) {
                val responseId = ad.responseId ?: return
                if (!container.isAttachedToWindow) return
                container.tag = responseId
                TapsellPlus.showStandardBannerAd(
                    container.context as? Activity ?: return,
                    responseId,
                    container,
                    object : AdShowListener() {
                        override fun onError(error: TapsellPlusErrorModel) {
                            container.removeAllViews()
                        }
                    }
                )
            }

            override fun error(message: String) {
                container.removeAllViews()
            }
        }
    )
}
