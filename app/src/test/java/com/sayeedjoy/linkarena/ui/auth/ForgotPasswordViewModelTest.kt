package com.sayeedjoy.linkarena.ui.auth

import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.util.MainDispatcherRule
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sendResetEmail_invalidEmail_setsValidationErrorWithoutCallingRepository() {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository)

        viewModel.onEmailChange("invalid-email")
        viewModel.sendResetEmail()

        assertEquals("Please enter a valid email address", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, repository.forgotPasswordCallCount)
    }

    @Test
    fun sendResetEmail_success_setsSuccessState() = runTest {
        val repository = FakeAuthRepository(
            forgotPasswordResult = NetworkResult.Success("Reset email sent")
        )
        val viewModel = ForgotPasswordViewModel(repository)

        viewModel.onEmailChange("user@example.com")
        viewModel.sendResetEmail()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
        assertEquals(1, repository.forgotPasswordCallCount)
    }

    @Test
    fun sendResetEmail_error_setsErrorState() = runTest {
        val repository = FakeAuthRepository(
            forgotPasswordResult = NetworkResult.Error("Email not found")
        )
        val viewModel = ForgotPasswordViewModel(repository)

        viewModel.onEmailChange("user@example.com")
        viewModel.sendResetEmail()
        advanceUntilIdle()

        assertEquals("Email not found", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, repository.forgotPasswordCallCount)
    }

    private class FakeAuthRepository(
        private val forgotPasswordResult: NetworkResult<String> = NetworkResult.Success("ok")
    ) : AuthRepository {

        var forgotPasswordCallCount: Int = 0

        override val isLoggedIn: Flow<Boolean> = flowOf(false)
        override val userEmail: Flow<String?> = flowOf(null)
        override val userName: Flow<String?> = flowOf(null)

        override suspend fun signIn(email: String, password: String): NetworkResult<Unit> {
            return NetworkResult.Success(Unit)
        }

        override suspend fun signUp(email: String, password: String, name: String): NetworkResult<Unit> {
            return NetworkResult.Success(Unit)
        }

        override suspend fun forgotPassword(email: String): NetworkResult<String> {
            forgotPasswordCallCount++
            return forgotPasswordResult
        }

        override suspend fun resetPassword(token: String, password: String): NetworkResult<Unit> {
            return NetworkResult.Success(Unit)
        }

        override suspend fun logout() = Unit

        override suspend fun getSession(): NetworkResult<Unit> {
            return NetworkResult.Success(Unit)
        }
    }
}
