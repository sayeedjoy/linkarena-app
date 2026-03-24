package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class MoveBookmarkToGroupUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarkId: String, groupId: String?): NetworkResult<Unit> {
        if (bookmarkId.isBlank()) {
            return NetworkResult.Error("Bookmark id is required")
        }
        return bookmarkRepository.moveBookmarkToGroup(bookmarkId, groupId)
    }
}
