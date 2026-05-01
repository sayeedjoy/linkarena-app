package com.sayeedjoy.linkarena.ads

object AdUnitIds {
    fun banner(): String? =
        if (AdConfigManager.adsEnabled && !AdConfigManager.isPremium) AdConfigManager.banner else null

    fun interstitial(): String? =
        if (AdConfigManager.adsEnabled && !AdConfigManager.isPremium) AdConfigManager.interstitial else null

    fun appOpen(): String? =
        if (AdConfigManager.adsEnabled && !AdConfigManager.isPremium) AdConfigManager.appOpen else null

    fun native(): String? =
        if (AdConfigManager.adsEnabled && !AdConfigManager.isPremium) AdConfigManager.nativeAd else null

    fun rewarded(): String? =
        if (AdConfigManager.adsEnabled && !AdConfigManager.isPremium) AdConfigManager.rewarded else null
}
