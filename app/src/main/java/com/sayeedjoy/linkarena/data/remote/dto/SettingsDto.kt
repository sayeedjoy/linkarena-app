package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsDto(
    val autoGroupEnabled: Boolean = false
)

@Serializable
data class UpdateSettingsRequest(
    val autoGroupEnabled: Boolean
)

@Serializable
data class SettingsResponse(
    val settings: UserSettingsDto? = null,
    val error: String? = null
)
