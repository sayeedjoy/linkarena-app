package com.sayeedjoy.linkarena.ads

import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi

object AdConfigManager {
    @Volatile var adsEnabled: Boolean = false
    @Volatile var appId: String? = null
    @Volatile var banner: String? = null
    @Volatile var interstitial: String? = null
    @Volatile var appOpen: String? = null
    @Volatile var rewarded: String? = null
    @Volatile var nativeAd: String? = null
    @Volatile var isPremium: Boolean = false
    @Volatile var planDisplayName: String? = null
    @Volatile var groupColoringAllowed: Boolean = true

    suspend fun fetch(api: LinkArenaApi) {
        try {
            val response = api.getAdsConfig()
            if (response.isSuccessful) {
                val config = response.body()
                val androidConfig = config?.admob?.android
                adsEnabled = config?.adsEnabled == true
                appId = androidConfig?.appId.sanitized()
                banner = androidConfig?.bannerId.sanitized()
                interstitial = androidConfig?.interstitialId.sanitized()
                appOpen = androidConfig?.appOpenId.sanitized()
                rewarded = androidConfig?.rewardedId.sanitized()
                nativeAd = androidConfig?.nativeId.sanitized()
            } else {
                disableAds()
            }
        } catch (_: Exception) {
            disableAds()
        }
    }

    suspend fun fetchSettings(api: LinkArenaApi) {
        try {
            val response = api.getSettings()
            if (response.isSuccessful) {
                val settings = response.body()
                val slug = settings?.plan?.slug.orEmpty()
                isPremium = slug == "premium"
                planDisplayName = settings?.plan?.displayName?.takeIf { it.isNotBlank() }
                groupColoringAllowed = settings?.groupColoringAllowed != false
                if (isPremium) disableAds()
            }
        } catch (_: Exception) {
            // keep current state
        }
    }

    fun isAdsSdkReady(): Boolean = adsEnabled && !appId.isNullOrBlank()

    private fun String?.sanitized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun disableAds() {
        adsEnabled = false
        appId = null
        banner = null
        interstitial = null
        appOpen = null
        rewarded = null
        nativeAd = null
    }
}
