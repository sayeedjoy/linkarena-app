package com.sayeedjoy.linkarena.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sayeedjoy.linkarena.data.local.db.entity.BookmarkEntity
import com.sayeedjoy.linkarena.data.local.db.entity.GroupEntity

@Database(
    entities = [BookmarkEntity::class, GroupEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LinkArenaDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun groupDao(): GroupDao
}
