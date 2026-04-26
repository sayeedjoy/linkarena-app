package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdsConfigDto(
    val adsEnabled: Boolean = false,
    val admob: AdmobConfigDto? = null
)

@Serializable
data class AdmobConfigDto(
    val android: AdmobAndroidConfigDto? = null
)

@Serializable
data class AdmobAndroidConfigDto(
    val appId: String? = null,
    val bannerId: String? = null,
    val interstitialId: String? = null,
    val appOpenId: String? = null,
    val rewardedId: String? = null,
    val nativeId: String? = null
)
