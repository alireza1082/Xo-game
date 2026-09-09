package com.example.android.xo

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.android.xo.ads.AdManager
import com.example.android.xo.ads.AdManagerFactory
import com.example.android.xo.ads.SentryAdMonitoring
import java.lang.ref.WeakReference

class XoApplication : Application() {
    lateinit var ads: AdManager
        private set

    private var resumedActivity: WeakReference<Activity> = WeakReference(null)

    override fun onCreate() {
        super.onCreate()
        SentryAdMonitoring.initialize(this, BuildConfig.SENTRY_DSN)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                resumedActivity = WeakReference(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (resumedActivity.get() === activity) resumedActivity.clear()
            }

            override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
        ads = AdManagerFactory.create(this) { resumedActivity.get() }
    }
}
