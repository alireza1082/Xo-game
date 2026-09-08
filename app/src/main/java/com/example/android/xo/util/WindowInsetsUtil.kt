package com.example.android.xo.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Applies system bar insets as padding on the root view.
 *
 * On Android 15+ (edge-to-edge enforced for targetSdk 35+) the content window
 * draws behind the status and navigation bars, so the root view must add
 * padding for those insets. On older platforms the window decor already fits
 * the system windows and no insets reach the content, so nothing changes.
 */
object WindowInsetsUtil {

    fun applySystemBarInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                maxOf(view.paddingTop, bars.top),
                view.paddingRight,
                maxOf(view.paddingBottom, bars.bottom)
            )
            WindowInsetsCompat.CONSUMED
        }
    }
}