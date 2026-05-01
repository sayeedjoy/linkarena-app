package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingsDto(
    val autoGroupEnabled: Boolean = false,
    val aiGroupingAllowed: Boolean = false,
    val groupColoringAllowed: Boolean = false,
    val planSource: String? = null,
    val plan: PlanDto? = null
)

@Serializable
data class PlanDto(
    val slug: String = "",
    val displayName: String = ""
)

@Serializable
data class UpdateSettingsRequest(
    val autoGroupEnabled: Boolean
)
