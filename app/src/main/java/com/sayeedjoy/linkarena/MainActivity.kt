package com.sayeedjoy.linkarena

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.sayeedjoy.linkarena.domain.model.ThemeMode
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.domain.repository.ThemePreferencesRepository
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator
import com.sayeedjoy.linkarena.ui.navigation.AuthNavGraph
import com.sayeedjoy.linkarena.ui.navigation.MainNavGraph
import com.sayeedjoy.linkarena.ui.theme.LinkArenaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var themePreferencesRepository: ThemePreferencesRepository

    @Inject
    lateinit var bookmarkRepository: BookmarkRepository

    @Inject
    lateinit var groupRepository: GroupRepository

    private var pendingSharedUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingShareIntent(intent)
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
                    LinkArenaApp(
                        authRepository = authRepository,
                        bookmarkRepository = bookmarkRepository,
                        groupRepository = groupRepository,
                        sharedUrl = pendingSharedUrl,
                        onSharedUrlConsumed = { pendingSharedUrl = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShareIntent(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        val sharedUrl = intent.extractSharedUrl()
        when {
            sharedUrl != null -> pendingSharedUrl = sharedUrl
            intent?.action == Intent.ACTION_SEND -> {
                Toast.makeText(this, "No valid link found in shared content", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun LinkArenaApp(
    authRepository: AuthRepository,
    bookmarkRepository: BookmarkRepository,
    groupRepository: GroupRepository,
    sharedUrl: String? = null,
    onSharedUrlConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val isLoggedIn by authRepository.isLoggedIn
        .map<Boolean, Boolean?> { it }
        .collectAsState(initial = null)

    when (isLoggedIn) {
        null -> LoadingIndicator()
        true -> {
            LaunchedRealtimeSync(
                bookmarkRepository = bookmarkRepository,
                groupRepository = groupRepository
            )
            MainNavGraph(
                navController = navController,
                onLogout = {},
                sharedUrl = sharedUrl,
                onSharedUrlConsumed = onSharedUrlConsumed
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

@Composable
private fun LaunchedRealtimeSync(
    bookmarkRepository: BookmarkRepository,
    groupRepository: GroupRepository
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (isActive) {
            withContext(Dispatchers.IO) {
                bookmarkRepository.syncBookmarks()
                groupRepository.syncGroups()
            }
            delay(5000)
        }
    }
}

private fun Intent?.extractSharedUrl(): String? {
    if (this?.action != Intent.ACTION_SEND) return null

    val shareText = buildString {
        append(getStringExtra(Intent.EXTRA_TEXT).orEmpty())
        append(' ')
        append(getStringExtra(Intent.EXTRA_SUBJECT).orEmpty())
    }.trim()
    val urlCandidate = shareText.extractFirstUrlCandidate() ?: return null
    return urlCandidate.normalizeSharedUrl()
}

private fun String.extractFirstUrlCandidate(): String? {
    if (isBlank()) return null

    return split("\\s+".toRegex())
        .asSequence()
        .map { it.trimUrlPunctuation() }
        .firstOrNull { token ->
            token.startsWith("http://", ignoreCase = true) ||
                token.startsWith("https://", ignoreCase = true) ||
                token.startsWith("www.", ignoreCase = true) ||
                token.looksLikeDomain()
        }
}

private fun String.normalizeSharedUrl(): String? {
    val candidate = when {
        startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true) -> this
        else -> "https://$this"
    }

    return try {
        val uri = android.net.Uri.parse(candidate)
        if (!uri.host.isNullOrBlank()) candidate else null
    } catch (e: Exception) {
        null
    }
}

private fun String.looksLikeDomain(): Boolean {
    if (contains("@")) return false
    return Regex("^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+(/.*)?$").matches(this)
}

private fun String.trimUrlPunctuation(): String {
    return trim()
        .trim(',', '.', ';', ':', '!', '?', ')', ']', '}', '>', '"', '\'')
        .trimStart('(', '[', '{', '<', '"', '\'')
}
