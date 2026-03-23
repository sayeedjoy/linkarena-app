package com.sayeedjoy.linkarena.data.repository

import com.sayeedjoy.linkarena.data.local.datastore.PreferencesManager
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.data.remote.dto.*
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: LinkArenaApi,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = preferencesManager.isLoggedIn
    override val userEmail: Flow<String?> = preferencesManager.userEmail
    override val userName: Flow<String?> = preferencesManager.userName

    override suspend fun signIn(email: String, password: String): NetworkResult<Unit> {
        return try {
            preferencesManager.clearAuthToken()

            val response = api.signIn(SignInRequest(email = email, password = password))
            if (response.isSuccessful && response.body()?.token != null) {
                val body = response.body()!!
                preferencesManager.saveAuthToken(body.token!!)
                body.user?.let {
                    preferencesManager.saveUser(it.id, it.email, it.name)
                }
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Sign in failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): NetworkResult<Unit> {
        return try {
            val response = api.signUp(SignUpRequest(email, password, name))
            if (response.isSuccessful && response.body()?.token != null) {
                val body = response.body()!!
                preferencesManager.saveAuthToken(body.token!!)
                body.user?.let {
                    preferencesManager.saveUser(it.id, it.email, it.name)
                }
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Sign up failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun forgotPassword(email: String): NetworkResult<String> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                NetworkResult.Success(response.body()?.message ?: "Reset email sent")
            } else {
                NetworkResult.Error(response.body()?.error ?: "Request failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun resetPassword(token: String, password: String): NetworkResult<Unit> {
        return try {
            val response = api.resetPassword(ResetPasswordRequest(token, password))
            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Reset failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun logout() {
        preferencesManager.clearSession()
    }

    override suspend fun getSession(): NetworkResult<Unit> {
        return try {
            val response = api.getSession()
            if (response.isSuccessful && response.body()?.user != null) {
                val user = response.body()!!.user!!
                preferencesManager.clearAuthToken()
                preferencesManager.saveUser(user.id, user.email, user.name)
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Session invalid")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }
}
