package com.sayeedjoy.linkarena.ui.splash

import com.sayeedjoy.linkarena.util.NetworkResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashSessionPolicyTest {

    @Test
    fun shouldForceLogoutFromSessionCheck_returnsTrue_forUnauthorizedError() {
        val result = NetworkResult.Error("Authentication expired. Please sign in again.")

        assertTrue(shouldForceLogoutFromSessionCheck(result))
    }

    @Test
    fun shouldForceLogoutFromSessionCheck_returnsFalse_forNetworkError() {
        val result = NetworkResult.Error("timeout")

        assertFalse(shouldForceLogoutFromSessionCheck(result))
    }

    @Test
    fun shouldForceLogoutFromSessionCheck_returnsFalse_forSuccess() {
        val result = NetworkResult.Success(Unit)

        assertFalse(shouldForceLogoutFromSessionCheck(result))
    }
}
