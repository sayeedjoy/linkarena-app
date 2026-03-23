package com.sayeedjoy.linkarena.domain.model

data class Bookmark(
    val id: String,
    val url: String?,
    val title: String?,
    val description: String?,
    val faviconUrl: String?,
    val previewImageUrl: String?,
    val groupId: String?,
    val createdAt: String,
    val updatedAt: String
)
