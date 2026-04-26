package com.sayeedjoy.linkarena.ads

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun load(activity: Activity) {
        val adUnitId = AdUnitIds.rewarded()
        if (adUnitId.isNullOrBlank()) {
            rewardedAd = null
            isLoading = false
            return
        }
        if (isLoading || rewardedAd != null) return

        isLoading = true
        RewardedAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    fun show(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = rewardedAd
        if (ad == null) {
            load(activity)
            onDismissed()
            return
        }

        rewardedAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                load(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                load(activity)
                onDismissed()
            }
        }
        ad.show(activity) {
            // This app does not currently have a premium/reward economy to update.
        }
    }
}
