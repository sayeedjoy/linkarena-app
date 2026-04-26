package com.sayeedjoy.linkarena.ads

internal object FullScreenAdCoordinator {
    @Volatile var isAdShowing: Boolean = false
    @Volatile var lastDismissedAt: Long = 0L

    fun onAdShowing() {
        isAdShowing = true
    }

    fun onAdDismissed() {
        isAdShowing = false
        lastDismissedAt = System.currentTimeMillis()
    }

    fun canShowAppOpen(): Boolean {
        if (isAdShowing) return false
        return System.currentTimeMillis() - lastDismissedAt > 2_000L
    }
}
