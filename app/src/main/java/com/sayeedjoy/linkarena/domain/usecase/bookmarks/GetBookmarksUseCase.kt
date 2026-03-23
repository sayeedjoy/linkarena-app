package com.sayeedjoy.linkarena.domain.usecase.bookmarks

import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarks()
    }

    fun byGroup(groupId: String): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarksByGroup(groupId)
    }

    fun search(query: String): Flow<List<Bookmark>> {
        return bookmarkRepository.searchBookmarks(query)
    }
}
