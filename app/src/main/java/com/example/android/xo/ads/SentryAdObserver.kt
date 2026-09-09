package com.example.android.xo.ads

import android.app.Application
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

object SentryAdMonitoring {
    fun initialize(application: Application, dsn: String?) {
        val configuredDsn = dsn?.trim().orEmpty()
        if (configuredDsn.isEmpty()) return

        runCatching {
            SentryAndroid.init(application) { options ->
                options.dsn = configuredDsn
                options.isSendDefaultPii = false
                options.isEnableAutoSessionTracking = false
            }
        }
    }

    fun observer(): AdObserver = object : AdObserver {
        override fun breadcrumb(message: String) {
            Sentry.addBreadcrumb(Breadcrumb().apply {
                category = "ads"
                this.message = message
            })
        }
    }
}
