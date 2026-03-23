package com.sayeedjoy.linkarena.domain.repository

import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun getBookmarks(): Flow<List<Bookmark>>
    fun getBookmarksByGroup(groupId: String): Flow<List<Bookmark>>
    fun searchBookmarks(query: String): Flow<List<Bookmark>>
    suspend fun getBookmarkById(id: String): Bookmark?
    suspend fun createBookmark(url: String, title: String?, description: String?, groupId: String?): NetworkResult<Bookmark>
    suspend fun updateBookmark(url: String, title: String?, description: String?, groupId: String?): NetworkResult<Bookmark>
    suspend fun deleteBookmark(id: String): NetworkResult<Unit>
    suspend fun moveBookmarkToGroup(bookmarkId: String, groupId: String?): NetworkResult<Unit>
    suspend fun syncBookmarks(): NetworkResult<Unit>
}
