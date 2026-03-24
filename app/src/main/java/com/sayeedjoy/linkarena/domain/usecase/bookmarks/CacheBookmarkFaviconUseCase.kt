package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import javax.inject.Inject

class CacheBookmarkFaviconUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarkId: String, faviconUrl: String) {
        if (bookmarkId.isBlank() || faviconUrl.isBlank()) return
        bookmarkRepository.cacheBookmarkFavicon(bookmarkId, faviconUrl)
    }
}
