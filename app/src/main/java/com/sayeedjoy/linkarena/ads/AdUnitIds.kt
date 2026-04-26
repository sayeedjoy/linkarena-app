package com.sayeedjoy.linkarena.ads

object AdUnitIds {
    fun banner(): String? =
        if (AdConfigManager.adsEnabled) AdConfigManager.banner else null

    fun interstitial(): String? =
        if (AdConfigManager.adsEnabled) AdConfigManager.interstitial else null

    fun appOpen(): String? =
        if (AdConfigManager.adsEnabled) AdConfigManager.appOpen else null

    fun native(): String? =
        if (AdConfigManager.adsEnabled) AdConfigManager.nativeAd else null

    fun rewarded(): String? =
        if (AdConfigManager.adsEnabled) AdConfigManager.rewarded else null
}
