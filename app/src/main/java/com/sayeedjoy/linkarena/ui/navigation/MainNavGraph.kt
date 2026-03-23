package com.sayeedjoy.linkarena.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sayeedjoy.linkarena.ui.bookmark.AddBookmarkScreen
import com.sayeedjoy.linkarena.ui.bookmark.BookmarkDetailScreen
import com.sayeedjoy.linkarena.ui.groups.GroupsScreen
import com.sayeedjoy.linkarena.ui.home.HomeScreen
import com.sayeedjoy.linkarena.ui.settings.SettingsScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddBookmark = {
                    navController.navigate(Screen.AddBookmark.route)
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

        composable(Screen.AddBookmark.route) {
            AddBookmarkScreen(
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
