package com.sayeedjoy.linkarena.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.DeleteBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.GetBookmarksUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.MoveBookmarkToGroupUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.RefetchBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.SyncBookmarksUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.GetGroupsUseCase
import com.sayeedjoy.linkarena.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val groups: List<Group> = emptyList(),
    val selectedGroupId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val moveBookmarkToGroupUseCase: MoveBookmarkToGroupUseCase,
    private val refetchBookmarkUseCase: RefetchBookmarkUseCase,
    private val syncBookmarksUseCase: SyncBookmarksUseCase,
    private val getGroupsUseCase: GetGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGroupId = MutableStateFlow<String?>(null)

    init {
        loadData()
        observeBookmarks()
        observeGroups()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = syncBookmarksUseCase()) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            combine(_searchQuery, _selectedGroupId) { query, groupId ->
                Pair(query, groupId)
            }.collectLatest { (query, groupId) ->
                val bookmarksFlow = when {
                    query.isNotBlank() -> getBookmarksUseCase.search(query)
                    groupId != null -> getBookmarksUseCase.byGroup(groupId)
                    else -> getBookmarksUseCase()
                }

                bookmarksFlow.collect { bookmarks ->
                    _uiState.value = _uiState.value.copy(bookmarks = bookmarks)
                }
            }
        }
    }

    private fun observeGroups() {
        viewModelScope.launch {
            getGroupsUseCase().collect { groups ->
                _uiState.value = _uiState.value.copy(groups = groups)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onGroupSelected(groupId: String?) {
        _selectedGroupId.value = groupId
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            when (val result = syncBookmarksUseCase()) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            when (val result = deleteBookmarkUseCase(id)) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun moveBookmarkToGroup(bookmarkId: String, groupId: String?) {
        viewModelScope.launch {
            when (val result = moveBookmarkToGroupUseCase(bookmarkId, groupId)) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun refetchBookmark(bookmarkId: String) {
        viewModelScope.launch {
            when (val result = refetchBookmarkUseCase(bookmarkId)) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
