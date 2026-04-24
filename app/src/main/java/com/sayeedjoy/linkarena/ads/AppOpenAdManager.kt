package com.sayeedjoy.linkarena.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AppOpenAdManager : Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var isShowing = false

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    private fun load(activity: Activity) {
        if (isLoading || appOpenAd != null) return

        isLoading = true
        AppOpenAd.load(
            activity,
            AdUnitIds.appOpen(activity),
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
        if (isShowing || ad == null) {
            load(activity)
            return
        }

        appOpenAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                load(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                isShowing = false
                load(activity)
            }
        }
        isShowing = true
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
