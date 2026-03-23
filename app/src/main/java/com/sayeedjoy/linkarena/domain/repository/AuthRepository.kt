package com.sayeedjoy.linkarena.domain.repository

import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val userEmail: Flow<String?>
    val userName: Flow<String?>

    suspend fun signIn(email: String, password: String): NetworkResult<Unit>
    suspend fun signUp(email: String, password: String, name: String): NetworkResult<Unit>
    suspend fun forgotPassword(email: String): NetworkResult<String>
    suspend fun resetPassword(token: String, password: String): NetworkResult<Unit>
    suspend fun logout()
    suspend fun getSession(): NetworkResult<Unit>
}
