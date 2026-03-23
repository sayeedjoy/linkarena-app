package com.sayeedjoy.linkarena.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: String?,
    val order: Int,
    @ColumnInfo(name = "bookmark_count")
    val bookmarkCount: Int = 0
)
