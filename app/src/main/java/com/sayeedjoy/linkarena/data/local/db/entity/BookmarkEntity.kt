package com.sayeedjoy.linkarena.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val url: String?,
    val title: String?,
    val description: String?,
    @ColumnInfo(name = "favicon_url")
    val faviconUrl: String?,
    @ColumnInfo(name = "preview_image_url")
    val previewImageUrl: String?,
    @ColumnInfo(name = "group_id")
    val groupId: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
