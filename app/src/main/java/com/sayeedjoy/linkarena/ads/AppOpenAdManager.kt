package com.sayeedjoy.linkarena.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AppOpenAdManager : Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    private fun load(activity: Activity) {
        val adUnitId = AdUnitIds.appOpen()
        if (adUnitId.isNullOrBlank()) {
            appOpenAd = null
            isLoading = false
            return
        }
        if (isLoading || appOpenAd != null) return

        isLoading = true
        AppOpenAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isLoading = false
                }
            }
        )
    }

    private fun showIfAvailable(activity: Activity) {
        val ad = appOpenAd
        if (!FullScreenAdCoordinator.canShowAppOpen() || ad == null) {
            load(activity)
            return
        }

        appOpenAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                FullScreenAdCoordinator.onAdShowing()
            }

            override fun onAdDismissedFullScreenContent() {
                FullScreenAdCoordinator.onAdDismissed()
                load(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                FullScreenAdCoordinator.onAdDismissed()
                load(activity)
            }
        }
        ad.show(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        showIfAvailable(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }
}
