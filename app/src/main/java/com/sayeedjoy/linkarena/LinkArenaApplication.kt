package com.sayeedjoy.linkarena

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.sayeedjoy.linkarena.ads.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LinkArenaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        AppOpenAdManager.register(this)
    }
}
