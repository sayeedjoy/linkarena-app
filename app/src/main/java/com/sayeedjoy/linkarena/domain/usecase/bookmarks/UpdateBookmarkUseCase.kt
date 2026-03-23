package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class UpdateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(url: String, title: String?, description: String?, groupId: String?): NetworkResult<Bookmark> {
        if (url.isBlank()) {
            return NetworkResult.Error("URL is required")
        }
        return bookmarkRepository.updateBookmark(url, title, description, groupId)
    }
}
