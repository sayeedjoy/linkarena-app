package com.sayeedjoy.linkarena.data.repository

import com.sayeedjoy.linkarena.data.local.db.BookmarkDao
import com.sayeedjoy.linkarena.data.local.db.GroupDao
import com.sayeedjoy.linkarena.data.local.db.entity.BookmarkEntity
import com.sayeedjoy.linkarena.data.local.db.entity.GroupEntity
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.data.remote.dto.BookmarkDto
import com.sayeedjoy.linkarena.data.remote.dto.CreateBookmarkRequest
import com.sayeedjoy.linkarena.data.remote.dto.GroupDto
import com.sayeedjoy.linkarena.data.remote.dto.UpdateBookmarkCategoryRequest
import com.sayeedjoy.linkarena.data.remote.dto.UpdateBookmarkRequest
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val api: LinkArenaApi,
    private val bookmarkDao: BookmarkDao,
    private val groupDao: GroupDao
) : BookmarkRepository {

    override fun getBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBookmarksByGroup(groupId: String): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByGroup(groupId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchBookmarks(query: String): Flow<List<Bookmark>> {
        return bookmarkDao.searchBookmarks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBookmarkById(id: String): Bookmark? {
        return bookmarkDao.getBookmarkById(id)?.toDomain()
    }

    override suspend fun createBookmark(url: String, title: String?, description: String?, groupId: String?): NetworkResult<Bookmark> {
        return try {
            val response = api.createBookmark(CreateBookmarkRequest(url, title, description, groupId))
            val resolvedBookmark = response.body()?.resolvedBookmark()
            if (response.isSuccessful && resolvedBookmark != null) {
                val bookmark = resolvedBookmark.toDomain()
                bookmarkDao.insert(bookmark.toEntity())
                NetworkResult.Success(bookmark)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Create failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateBookmark(url: String, title: String?, description: String?, groupId: String?): NetworkResult<Bookmark> {
        return try {
            val response = api.updateBookmark(UpdateBookmarkRequest(url, title, description, groupId))
            val resolvedBookmark = response.body()?.resolvedBookmark()
            if (response.isSuccessful && resolvedBookmark != null) {
                val bookmark = resolvedBookmark.toDomain()
                bookmarkDao.update(bookmark.toEntity())
                NetworkResult.Success(bookmark)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Update failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun deleteBookmark(id: String): NetworkResult<Unit> {
        return try {
            val response = api.deleteBookmarkById(id)
            if (response.isSuccessful) {
                bookmarkDao.deleteById(id)
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Delete failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun refetchBookmark(bookmarkId: String): NetworkResult<Bookmark> {
        return try {
            val response = api.refetchBookmark(bookmarkId)
            val resolvedBookmark = response.body()?.resolvedBookmark()
            if (response.isSuccessful && resolvedBookmark != null) {
                val bookmark = resolvedBookmark.toDomain()
                bookmarkDao.update(bookmark.toEntity())
                NetworkResult.Success(bookmark)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Refetch failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun moveBookmarkToGroup(bookmarkId: String, groupId: String?): NetworkResult<Unit> {
        return try {
            val response = api.updateBookmarkCategory(bookmarkId, UpdateBookmarkCategoryRequest(groupId))
            if (response.isSuccessful) {
                val bookmark = bookmarkDao.getBookmarkById(bookmarkId)
                bookmark?.let {
                    bookmarkDao.update(it.copy(groupId = groupId))
                }
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Move failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun cacheBookmarkFavicon(bookmarkId: String, faviconUrl: String) {
        if (faviconUrl.isBlank()) return
        bookmarkDao.updateFaviconUrl(bookmarkId, faviconUrl)
    }

    override suspend fun syncBookmarks(): NetworkResult<Unit> {
        return try {
            val initialResponse = api.sync(mode = "initial")
            if (!initialResponse.isSuccessful) {
                if (initialResponse.code() == 401) {
                    return NetworkResult.Error("Authentication expired. Please sign in again.")
                }
                val serverMessage = initialResponse.errorBody()?.string()?.take(400)
                val message = buildString {
                    append("Sync failed (HTTP ")
                    append(initialResponse.code())
                    append(")")
                    if (!serverMessage.isNullOrBlank()) {
                        append(": ")
                        append(serverMessage)
                    }
                }
                return NetworkResult.Error(message)
            }

            val initial = initialResponse.body()!!
            val allBookmarks = mutableListOf<BookmarkEntity>()
            allBookmarks.addAll(initial.bookmarks.map { it.toDomain().toEntity() })
            val allGroups = mutableListOf<GroupEntity>()
            allGroups.addAll(initial.groups.map { it.toEntity() })

            var hasMore = initial.hasMore
            var cursor = initial.nextCursor
            while (hasMore && cursor != null) {
                val pageResponse = api.sync(cursor = cursor)
                if (pageResponse.isSuccessful) {
                    val page = pageResponse.body()!!
                    allBookmarks.addAll(page.bookmarks.map { it.toDomain().toEntity() })
                    allGroups.addAll(page.groups.map { it.toEntity() })
                    hasMore = page.hasMore
                    cursor = page.nextCursor
                } else {
                    if (pageResponse.code() == 401) {
                        return NetworkResult.Error("Authentication expired. Please sign in again.")
                    }
                    break
                }
            }

            val remoteBookmarks = allBookmarks.sortedBy { it.id }
            val localBookmarks = bookmarkDao.getAllBookmarksSnapshot().sortedBy { it.id }
            if (remoteBookmarks != localBookmarks) {
                // Replace local snapshot atomically to avoid transient empty-list emissions.
                bookmarkDao.replaceAll(allBookmarks)
            }

            val remoteGroups = allGroups.distinctBy { it.id }.sortedBy { it.id }
            val localGroups = groupDao.getAllGroupsSnapshot().sortedBy { it.id }
            if (remoteGroups != localGroups) {
                groupDao.replaceAll(remoteGroups)
            }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    private fun BookmarkEntity.toDomain() = Bookmark(
        id = id,
        url = url,
        title = title,
        description = description,
        faviconUrl = faviconUrl,
        previewImageUrl = previewImageUrl,
        groupId = groupId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Bookmark.toEntity() = BookmarkEntity(
        id = id,
        url = url,
        title = title,
        description = description,
        faviconUrl = faviconUrl,
        previewImageUrl = previewImageUrl,
        groupId = groupId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BookmarkDto.toDomain() = Bookmark(
        id = id,
        url = url,
        title = title,
        description = description,
        faviconUrl = faviconUrl,
        previewImageUrl = previewImageUrl,
        groupId = groupId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun GroupDto.toEntity() = GroupEntity(
        id = id,
        name = name,
        color = color,
        order = order,
        bookmarkCount = if (bookmarkCount != 0) bookmarkCount else (count?.bookmarks ?: 0)
    )
}
