package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val color: String? = null,
    val order: Int = 0,
    @SerialName("bookmarkCount")
    val bookmarkCount: Int = 0
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val color: String? = null
)

@Serializable
data class UpdateGroupRequest(
    val name: String? = null,
    val color: String? = null,
    val order: Int? = null
)

@Serializable
data class GroupResponse(
    val group: GroupDto? = null,
    val error: String? = null
)

@Serializable
data class GroupListResponse(
    val groups: List<GroupDto> = emptyList(),
    val error: String? = null
)
