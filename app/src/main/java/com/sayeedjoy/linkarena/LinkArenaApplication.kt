package com.sayeedjoy.linkarena

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.sayeedjoy.linkarena.ads.AdConfigManager
import com.sayeedjoy.linkarena.ads.AppOpenAdManager
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltAndroidApp
class LinkArenaApplication : Application() {

    @Inject
    lateinit var linkArenaApi: LinkArenaApi

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            AdConfigManager.fetch(linkArenaApi)
            if (AdConfigManager.isAdsSdkReady()) {
                withContext(Dispatchers.Main) {
                    MobileAds.initialize(this@LinkArenaApplication)
                    AppOpenAdManager.register(this@LinkArenaApplication)
                }
            }
            authRepository.getSession()
            AdConfigManager.fetchSettings(linkArenaApi)
        }
    }
}
