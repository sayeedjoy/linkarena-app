package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class CreateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(url: String, title: String?, description: String?, groupId: String?): NetworkResult<Bookmark> {
        if (url.isBlank()) {
            return NetworkResult.Error("URL is required")
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return NetworkResult.Error("URL must start with http:// or https://")
        }
        return bookmarkRepository.createBookmark(url, title, description, groupId)
    }
}
