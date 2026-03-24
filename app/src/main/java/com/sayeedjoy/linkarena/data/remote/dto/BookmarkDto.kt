package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BookmarkDto(
    val id: String,
    val url: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("faviconUrl")
    val faviconUrl: String? = null,
    @SerialName("previewImageUrl")
    val previewImageUrl: String? = null,
    @SerialName("groupId")
    val groupId: String? = null,
    val group: JsonElement? = null,
    @SerialName("groupColor")
    val groupColor: String? = null,
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("updatedAt")
    val updatedAt: String = ""
)

@Serializable
data class CreateBookmarkRequest(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    @SerialName("groupId")
    val groupId: String? = null
)

@Serializable
data class UpdateBookmarkRequest(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    @SerialName("groupId")
    val groupId: String? = null
)

@Serializable
data class UpdateBookmarkCategoryRequest(
    @SerialName("categoryId")
    val categoryId: String?
)

@Serializable
data class DeleteBookmarkRequest(
    val url: String
)

@Serializable
data class BookmarkResponse(
    val bookmark: BookmarkDto? = null,
    val error: String? = null,
    val id: String? = null,
    val url: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("faviconUrl")
    val faviconUrl: String? = null,
    @SerialName("previewImageUrl")
    val previewImageUrl: String? = null,
    @SerialName("groupId")
    val groupId: String? = null,
    val group: JsonElement? = null,
    @SerialName("groupColor")
    val groupColor: String? = null,
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("updatedAt")
    val updatedAt: String = ""
) {
    fun resolvedBookmark(): BookmarkDto? {
        bookmark?.let { return it }
        val bookmarkId = id ?: return null
        return BookmarkDto(
            id = bookmarkId,
            url = url,
            title = title,
            description = description,
            faviconUrl = faviconUrl,
            previewImageUrl = previewImageUrl,
            groupId = groupId,
            group = group,
            groupColor = groupColor,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

@Serializable
data class BookmarkListResponse(
    val bookmarks: List<BookmarkDto> = emptyList(),
    val error: String? = null
)
