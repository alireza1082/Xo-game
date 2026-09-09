package com.example.android.xo.ads

import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.adivery.sdk.AdiveryBannerAdView
import com.adivery.sdk.BannerSize
import com.example.android.xo.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val provider = BuildConfig.AD_PROVIDER.lowercase()

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            FrameLayout(viewContext)
        },
        update = { container ->
            if (container.childCount > 0) return@AndroidView
            when (provider) {
                "adivery", "mixed" -> loadAdiveryBanner(container, context)
                "tapsell" -> loadTapsellBanner(container)
            }
        },
        onRelease = { container ->
            val activity = container.context as? android.app.Activity
            val responseId = container.tag as? String
            if (activity != null && !responseId.isNullOrBlank()) {
                runCatching { TapsellPlus.destroyStandardBanner(activity, responseId, container) }
            }
            container.removeAllViews()
        }
    )
}

private fun loadAdiveryBanner(container: FrameLayout, context: android.content.Context) {
    val placementId = BuildConfig.ADIVERY_BANNER_PLACEMENT
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
    val placementId = BuildConfig.TAPSELL_BANNER_PLACEMENT
    if (placementId.isBlank()) return
    TapsellPlus.requestStandardBannerAd(
        container.context as? android.app.Activity ?: return,
        placementId,
        TapsellPlusBannerType.BANNER_320x50,
        object : AdRequestCallback() {
            override fun response(ad: TapsellPlusAdModel) {
                val responseId = ad.responseId ?: return
                if (!container.isAttachedToWindow) return
                container.tag = responseId
                TapsellPlus.showStandardBannerAd(
                    container.context as? android.app.Activity ?: return,
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
