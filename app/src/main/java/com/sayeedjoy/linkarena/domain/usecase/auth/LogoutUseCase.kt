package com.sayeedjoy.linkarena.domain.usecase.auth

import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
