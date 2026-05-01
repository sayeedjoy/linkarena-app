package com.sayeedjoy.linkarena.ui.splash

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.MobileAds
import com.sayeedjoy.linkarena.ads.AdConfigManager
import com.sayeedjoy.linkarena.ads.AppOpenAdManager
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class SplashDestination {
    AUTH,
    MAIN
}

data class SplashUiState(
    val destination: SplashDestination? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val linkArenaApi: LinkArenaApi,
    @ApplicationContext private val appContext: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        startBootstrap()
    }

    private fun startBootstrap() {
        viewModelScope.launch(Dispatchers.IO) {
            // Non-critical startup work runs in background and never blocks navigation.
            launch { bootstrapAdsConfig() }

            val isLoggedIn = authRepository.isLoggedIn.first()
            _uiState.value = SplashUiState(
                destination = if (isLoggedIn) SplashDestination.MAIN else SplashDestination.AUTH
            )

            if (isLoggedIn) {
                launch { AdConfigManager.fetchSettings(linkArenaApi) }
                launch {
                    val sessionCheck = authRepository.getSession()
                    if (shouldForceLogoutFromSessionCheck(sessionCheck)) {
                        authRepository.logout()
                    }
                }
            }
        }
    }

    private suspend fun bootstrapAdsConfig() {
        AdConfigManager.fetch(linkArenaApi)
        if (AdConfigManager.isAdsSdkReady()) {
            withContext(Dispatchers.Main) {
                MobileAds.initialize(appContext)
                (appContext as? Application)?.let { AppOpenAdManager.register(it) }
            }
        }
    }
}

internal fun shouldForceLogoutFromSessionCheck(result: com.sayeedjoy.linkarena.util.NetworkResult<Unit>): Boolean {
    return result is com.sayeedjoy.linkarena.util.NetworkResult.Error &&
        result.message.equals("Authentication expired. Please sign in again.", ignoreCase = true)
}
