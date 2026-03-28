package com.sayeedjoy.linkarena.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.sayeedjoy.linkarena.ui.bookmark.AddBookmarkScreen
import com.sayeedjoy.linkarena.ui.bookmark.BookmarkDetailScreen
import com.sayeedjoy.linkarena.ui.groups.GroupDetailScreen
import com.sayeedjoy.linkarena.ui.groups.GroupsScreen
import com.sayeedjoy.linkarena.ui.home.HomeScreen
import com.sayeedjoy.linkarena.ui.settings.SettingsScreen

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

    val bottomBarDestinations = listOf(
        Screen.Home.route,
        Screen.Groups.route,
        Screen.Settings.route
    )

    val showBottomBar = currentRoute in bottomBarDestinations

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == Screen.Home.route,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Folder, contentDescription = "Groups") },
                        label = { Text("Groups", style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == Screen.Groups.route,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(Screen.Groups.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                        selected = currentRoute == Screen.Settings.route,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
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
                        navController.navigate(Screen.BookmarkDetail.createRoute(bookmarkId))
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
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout
                )
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
