package com.sayeedjoy.linkarena.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sayeedjoy.linkarena.ui.bookmark.AddBookmarkScreen
import com.sayeedjoy.linkarena.ui.bookmark.BookmarkDetailScreen
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

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddBookmark = {
                    navController.navigate(Screen.AddBookmark.createRoute())
                },
                onNavigateToBookmarkDetail = { bookmarkId ->
                    navController.navigate(Screen.BookmarkDetail.createRoute(bookmarkId))
                },
                onNavigateToGroups = {
                    navController.navigate(Screen.Groups.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = onLogout
            )
        }
    }
}
