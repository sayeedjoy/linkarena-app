package com.sayeedjoy.linkarena

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinkArenaTheme {
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
