package com.sayeedjoy.linkarena.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.BookmarkRepository
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.DeleteBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.UpdateBookmarkUseCase
import com.sayeedjoy.linkarena.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkDetailUiState(
    val bookmark: Bookmark? = null,
    val groups: List<Group> = emptyList(),
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val selectedGroupId: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class BookmarkDetailViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val groupRepository: GroupRepository,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkDetailUiState())
    val uiState: StateFlow<BookmarkDetailUiState> = _uiState.asStateFlow()

    init {
        observeGroups()
    }

    private fun observeGroups() {
        viewModelScope.launch {
            groupRepository.getGroups().collect { groups ->
                _uiState.value = _uiState.value.copy(groups = groups)
            }
        }
    }

    fun loadBookmark(bookmarkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val bookmark = bookmarkRepository.getBookmarkById(bookmarkId)
            if (bookmark != null) {
                _uiState.value = _uiState.value.copy(
                    bookmark = bookmark,
                    url = bookmark.url ?: "",
                    title = bookmark.title ?: "",
                    description = bookmark.description ?: "",
                    selectedGroupId = bookmark.groupId,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Bookmark not found"
                )
            }
        }
    }

    fun onUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(url = url)
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onGroupSelected(groupId: String?) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
    }

    fun saveBookmark() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            when (val result = updateBookmarkUseCase(
                _uiState.value.url,
                _uiState.value.title.ifBlank { null },
                _uiState.value.description.ifBlank { null },
                _uiState.value.selectedGroupId
            )) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isSaving = true)
                }
            }
        }
    }

    fun deleteBookmark() {
        val bookmarkId = _uiState.value.bookmark?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            when (val result = deleteBookmarkUseCase(bookmarkId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, isDeleted = true)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isSaving = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
