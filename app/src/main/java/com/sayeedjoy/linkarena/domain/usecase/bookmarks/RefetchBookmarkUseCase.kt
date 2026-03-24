package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.delay
import javax.inject.Inject

class RefetchBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarkId: String): NetworkResult<Bookmark> {
        if (bookmarkId.isBlank()) {
            return NetworkResult.Error("Bookmark id is required")
        }

        val initialBookmark = bookmarkRepository.getBookmarkById(bookmarkId)
            ?: return NetworkResult.Error("Bookmark not found")

        val normalizedUrl = normalizeUrl(initialBookmark.url)
            ?: return NetworkResult.Error("Bookmark URL not available")

        // Web app metadata refresh can happen asynchronously on the server.
        // Trigger refresh attempt, then poll sync for updated metadata/favicon.
        val updateResult = bookmarkRepository.updateBookmark(
            url = normalizedUrl,
            title = null,
            description = null,
            groupId = initialBookmark.groupId
        )

        if (updateResult is NetworkResult.Error) {
            bookmarkRepository.createBookmark(
                url = normalizedUrl,
                title = null,
                description = null,
                groupId = initialBookmark.groupId
            )
        }

        var lastSyncError: String? = null
        repeat(5) { attempt ->
            when (val syncResult = bookmarkRepository.syncBookmarks()) {
                is NetworkResult.Success -> {
                    val latestBookmark = bookmarkRepository.getBookmarkById(bookmarkId) ?: initialBookmark
                    if (hasNewMetadata(initialBookmark, latestBookmark) || attempt == 4) {
                        return NetworkResult.Success(latestBookmark)
                    }
                }
                is NetworkResult.Error -> {
                    lastSyncError = syncResult.message
                }
                is NetworkResult.Loading -> {}
            }
            delay(1200)
        }

        val latestBookmark = bookmarkRepository.getBookmarkById(bookmarkId) ?: initialBookmark
        return if (latestBookmark != initialBookmark) {
            NetworkResult.Success(latestBookmark)
        } else {
            NetworkResult.Error(lastSyncError ?: "Refetch failed")
        }
    }

    private fun normalizeUrl(url: String?): String? {
        val rawUrl = url?.trim().orEmpty()
        if (rawUrl.isBlank()) return null
        return if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "https://$rawUrl"
        }
    }

    private fun hasNewMetadata(before: Bookmark, after: Bookmark): Boolean {
        val gotTitle = before.title.isNullOrBlank() && !after.title.isNullOrBlank()
        val gotDescription = before.description.isNullOrBlank() && !after.description.isNullOrBlank()
        val gotFavicon = before.faviconUrl.isNullOrBlank() && !after.faviconUrl.isNullOrBlank()
        val gotPreview = before.previewImageUrl.isNullOrBlank() && !after.previewImageUrl.isNullOrBlank()
        return gotTitle || gotDescription || gotFavicon || gotPreview || before != after
    }
}
