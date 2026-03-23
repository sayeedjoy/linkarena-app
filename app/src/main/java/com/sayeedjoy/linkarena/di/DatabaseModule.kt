package com.sayeedjoy.linkarena.di

import android.content.Context
import androidx.room.Room
import com.sayeedjoy.linkarena.data.local.db.BookmarkDao
import com.sayeedjoy.linkarena.data.local.db.GroupDao
import com.sayeedjoy.linkarena.data.local.db.LinkArenaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LinkArenaDatabase {
        return Room.databaseBuilder(
            context,
            LinkArenaDatabase::class.java,
            "linkarena_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: LinkArenaDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: LinkArenaDatabase): GroupDao {
        return database.groupDao()
    }
}
