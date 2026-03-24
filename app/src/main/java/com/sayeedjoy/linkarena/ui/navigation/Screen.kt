package com.sayeedjoy.linkarena.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    // Auth screens
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object ResetPassword : Screen("reset_password/{token}") {
        fun createRoute(token: String) = "reset_password/$token"
    }

    // Main screens
    data object Home : Screen("home")
    data object AddBookmark : Screen("add_bookmark?sharedUrl={sharedUrl}") {
        const val BASE_ROUTE = "add_bookmark"
        const val ARG_SHARED_URL = "sharedUrl"

        fun createRoute(sharedUrl: String? = null): String {
            return if (sharedUrl.isNullOrBlank()) {
                BASE_ROUTE
            } else {
                "$BASE_ROUTE?$ARG_SHARED_URL=${Uri.encode(sharedUrl)}"
            }
        }
    }
    data object BookmarkDetail : Screen("bookmark_detail/{bookmarkId}") {
        fun createRoute(bookmarkId: String) = "bookmark_detail/$bookmarkId"
    }
    data object Groups : Screen("groups")
    data object Settings : Screen("settings")
}
