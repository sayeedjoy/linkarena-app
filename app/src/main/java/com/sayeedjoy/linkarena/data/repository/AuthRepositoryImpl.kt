package com.sayeedjoy.linkarena.data.repository

import com.sayeedjoy.linkarena.data.local.datastore.PreferencesManager
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.data.remote.dto.ForgotPasswordRequest
import com.sayeedjoy.linkarena.data.remote.dto.MessageResponse
import com.sayeedjoy.linkarena.data.remote.dto.ResetPasswordRequest
import com.sayeedjoy.linkarena.data.remote.dto.SignInRequest
import com.sayeedjoy.linkarena.data.remote.dto.SignUpRequest
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: LinkArenaApi,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = preferencesManager.isLoggedIn
    override val userEmail: Flow<String?> = preferencesManager.userEmail
    override val userName: Flow<String?> = preferencesManager.userName
    override val userPhotoUrl: Flow<String?> = preferencesManager.userPhotoUrl


    override suspend fun signIn(email: String, password: String): NetworkResult<Unit> {
        return try {
            preferencesManager.clearSession()

            val response = api.signIn(SignInRequest(email = email, password = password))
            if (response.isSuccessful && response.body()?.token != null) {
                val body = response.body()!!
                preferencesManager.saveAuthToken(body.token!!)
                val user = body.user
                preferencesManager.saveUser(
                    id = user?.id ?: email,
                    email = user?.email?.ifBlank { email } ?: email,
                    name = user?.name,
                    photoUrl = user?.photoUrl

                )
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
                val user = body.user
                preferencesManager.saveUser(
                    id = user?.id ?: email,
                    email = user?.email?.ifBlank { email } ?: email,
                    name = user?.name?.ifBlank { name } ?: name,
                    photoUrl = user?.photoUrl
                )
                NetworkResult.Success(Unit)
            } else {
                val errorMsg = response.body()?.error
                    ?: response.errorBody()?.string()?.let { raw ->
                        if (raw.isBlank() || (raw.contains('<') && raw.contains('>'))) null
                        else raw.take(300).trim()
                    }
                    ?: "Sign up failed (${response.code()})"
                NetworkResult.Error(errorMsg)
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
                NetworkResult.Error(response.resolveErrorMessage("Request failed"))
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
                NetworkResult.Error(response.resolveErrorMessage("Reset failed"))
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
                val existingName = preferencesManager.userName.first()
                preferencesManager.saveUser(
                    id = user.id,
                    email = user.email,
                    name = user.name ?: existingName,
                    photoUrl = user.photoUrl
                )
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Session invalid")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    private fun Response<MessageResponse>.resolveErrorMessage(fallback: String): String {
        val apiError = body()?.error?.takeIf { it.isNotBlank() }
        if (apiError != null) return apiError

        val rawError = errorBody()?.string()?.trim().orEmpty().take(400)
        if (rawError.isBlank()) return fallback

        return if (rawError.contains('<') && rawError.contains('>')) fallback else rawError
    }
}
