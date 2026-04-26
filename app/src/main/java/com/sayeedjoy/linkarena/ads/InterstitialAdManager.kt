package com.sayeedjoy.linkarena.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun load(activity: Activity) {
        if (isLoading || interstitialAd != null) return

        isLoading = true
        InterstitialAd.load(
            activity,
            AdUnitIds.interstitial(activity),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun show(activity: Activity, onDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            load(activity)
            onDismissed()
            return
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                FullScreenAdCoordinator.onAdShowing()
            }

            override fun onAdDismissedFullScreenContent() {
                FullScreenAdCoordinator.onAdDismissed()
                load(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                FullScreenAdCoordinator.onAdDismissed()
                load(activity)
                onDismissed()
            }
        }
        ad.show(activity)
    }
}
