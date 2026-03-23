package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponseDto(
    val bookmarks: List<BookmarkDto> = emptyList(),
    val groups: List<GroupDto> = emptyList(),
    val partial: Boolean = false,
    @SerialName("hasMore")
    val hasMore: Boolean = false,
    @SerialName("nextCursor")
    val nextCursor: String? = null
)
