package com.sayeedjoy.linkarena.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.sayeedjoy.linkarena.ads.BannerAd
import com.sayeedjoy.linkarena.ads.InterstitialAdManager
import com.sayeedjoy.linkarena.ads.rememberActivity
import com.sayeedjoy.linkarena.ui.components.ScreenSystemBars
import com.sayeedjoy.linkarena.ui.bookmark.AddBookmarkScreen
import com.sayeedjoy.linkarena.ui.bookmark.BookmarkDetailScreen
import com.sayeedjoy.linkarena.ui.groups.GroupDetailScreen
import com.sayeedjoy.linkarena.ui.groups.GroupsScreen
import com.sayeedjoy.linkarena.ui.home.HomeScreen
import com.sayeedjoy.linkarena.ui.premium.PremiumScreen
import com.sayeedjoy.linkarena.ui.settings.About
import com.sayeedjoy.linkarena.ui.settings.SettingsScreen

private data class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val BottomNavDestinations = listOf(
    BottomNavDestination(Screen.Home.route, "Home", Icons.Filled.Home),
    BottomNavDestination(Screen.Groups.route, "Groups", Icons.Filled.Folder),
    BottomNavDestination(Screen.Settings.route, "Settings", Icons.Filled.Settings)
)

@Composable
fun MainNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit,
    sharedUrl: String? = null,
    onSharedUrlConsumed: () -> Unit = {}
) {
    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            navController.navigate(Screen.AddBookmark.createRoute(sharedUrl)) {
                launchSingleTop = true
            }
            onSharedUrlConsumed()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = BottomNavDestinations.any { it.route == currentRoute }
    val activity = rememberActivity()
    val defaultBackground = MaterialTheme.colorScheme.background
    val tabBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
    val bottomBarColor = tabBackgroundColor
    val statusBarColor = when (currentRoute) {
        Screen.Home.route,
        Screen.Groups.route,
        Screen.Settings.route -> tabBackgroundColor
        Screen.About.route -> MaterialTheme.colorScheme.surface
        else -> defaultBackground
    }
    val navigationBarColor = if (showBottomBar) bottomBarColor else statusBarColor

    ScreenSystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor
    )

    LaunchedEffect(activity) {
        activity?.let(InterstitialAdManager::load)
    }

    Scaffold(
        containerColor = if (showBottomBar) tabBackgroundColor else defaultBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    destinations = BottomNavDestinations,
                    currentRoute = currentRoute,
                    onDestinationClick = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddBookmark = {
                        navController.navigate(Screen.AddBookmark.createRoute())
                    },
                    onNavigateToBookmarkDetail = { bookmarkId ->
                        activity?.let {
                            InterstitialAdManager.show(it) {
                                navController.navigate(Screen.BookmarkDetail.createRoute(bookmarkId))
                            }
                        } ?: navController.navigate(Screen.BookmarkDetail.createRoute(bookmarkId))
                    },
                    onNavigateToPremium = {
                        navController.navigate(Screen.Premium.route)
                    }
                )
            }

            composable(
                route = Screen.AddBookmark.route,
                arguments = listOf(
                    navArgument(Screen.AddBookmark.ARG_SHARED_URL) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val sharedUrlArg = backStackEntry.arguments?.getString(Screen.AddBookmark.ARG_SHARED_URL)
                AddBookmarkScreen(
                    initialUrl = sharedUrlArg,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onBookmarkCreated = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.BookmarkDetail.route) { backStackEntry ->
                val bookmarkId = backStackEntry.arguments?.getString("bookmarkId") ?: ""
                BookmarkDetailScreen(
                    bookmarkId = bookmarkId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Groups.route) {
                GroupsScreen(
                    onNavigateToGroupDetail = { groupId ->
                        activity?.let {
                            InterstitialAdManager.show(it) {
                                navController.navigate(Screen.GroupDetail.createRoute(groupId))
                            }
                        } ?: navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout,
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremium = {
                        navController.navigate(Screen.Premium.route)
                    }
                )
            }

            composable(Screen.Premium.route) {
                PremiumScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.About.route) {
                About(navController = navController)
            }

            composable(
                route = Screen.GroupDetail.route,
                arguments = listOf(
                    navArgument(Screen.GroupDetail.ARG_GROUP_ID) {
                        type = NavType.StringType
                    }
                )
            ) {
                GroupDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToBookmarkDetail = { bookmarkId ->
                        navController.navigate(Screen.BookmarkDetail.createRoute(bookmarkId))
                    }
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    destinations: List<BottomNavDestination>,
    currentRoute: String?,
    onDestinationClick: (String) -> Unit
) {
    val bottomBarColor = MaterialTheme.colorScheme.surfaceContainerLow
    val bottomBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column {
        BannerAd()
        NavigationBar(
            containerColor = bottomBarColor,
            contentColor = bottomBarContentColor,
            tonalElevation = 0.dp
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selected,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = bottomBarContentColor,
                        unselectedTextColor = bottomBarContentColor
                    ),
                    onClick = { onDestinationClick(destination.route) }
                )
            }
        }
    }
}
