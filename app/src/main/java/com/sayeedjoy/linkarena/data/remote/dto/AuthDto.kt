package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class AuthResponse(
    val token: String? = null,
    val user: UserDto? = null,
    val error: String? = null
)

@Serializable
data class UserDto(
    val id: String = "",
    val email: String = "",
    val name: String?
)

@Serializable
data class SessionResponse(
    val user: UserDto? = null,
    val error: String? = null
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val password: String
)

@Serializable
data class MessageResponse(
    val message: String? = null,
    val error: String? = null
)
