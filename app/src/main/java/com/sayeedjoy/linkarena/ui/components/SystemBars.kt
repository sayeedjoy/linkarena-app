package com.sayeedjoy.linkarena.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
@Suppress("DEPRECATION")
fun ScreenSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color = statusBarColor,
    lightStatusBars: Boolean? = null,
    lightNavigationBars: Boolean? = null
) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return
    val useDarkSystemIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val useLightStatusBars = lightStatusBars ?: useDarkSystemIcons
    val useLightNavigationBars = lightNavigationBars ?: useDarkSystemIcons

    SideEffect {
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, view)

        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()
        controller.isAppearanceLightStatusBars = useLightStatusBars
        controller.isAppearanceLightNavigationBars = useLightNavigationBars
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
