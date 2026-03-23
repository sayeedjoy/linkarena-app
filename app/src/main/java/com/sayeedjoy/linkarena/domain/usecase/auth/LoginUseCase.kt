package com.sayeedjoy.linkarena.domain.usecase.auth

import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<Unit> {
        if (email.isBlank()) {
            return NetworkResult.Error("Email is required")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return NetworkResult.Error("Invalid email format")
        }
        if (password.isBlank()) {
            return NetworkResult.Error("Password is required")
        }
        if (password.length < 6) {
            return NetworkResult.Error("Password must be at least 6 characters")
        }
        return authRepository.signIn(email, password)
    }
}
