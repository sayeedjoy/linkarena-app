package com.sayeedjoy.linkarena

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.sayeedjoy.linkarena.domain.model.ThemeMode
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.domain.repository.ThemePreferencesRepository
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator
import com.sayeedjoy.linkarena.ui.navigation.AuthNavGraph
import com.sayeedjoy.linkarena.ui.navigation.MainNavGraph
import com.sayeedjoy.linkarena.ui.theme.LinkArenaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isSystemDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }

            LinkArenaTheme(isDarkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LinkArenaApp(authRepository = authRepository)
                }
            }
        }
    }
}

@Composable
fun LinkArenaApp(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val isLoggedIn by authRepository.isLoggedIn
        .map<Boolean, Boolean?> { it }
        .collectAsState(initial = null)

    when (isLoggedIn) {
        null -> LoadingIndicator()
        true -> {
            MainNavGraph(
                navController = navController,
                onLogout = {}
            )
        }
        false -> {
            AuthNavGraph(
                navController = navController,
                onAuthSuccess = {}
            )
        }
    }
}
