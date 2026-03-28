package com.sayeedjoy.linkarena.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.CacheBookmarkFaviconUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.DeleteBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.GetBookmarksUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.MoveBookmarkToGroupUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.RefetchBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.CreateGroupUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.GetGroupsUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.SyncGroupsUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.UpdateGroupUseCase
import com.sayeedjoy.linkarena.ui.navigation.Screen
import com.sayeedjoy.linkarena.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: Group? = null,
    val groups: List<Group> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val moveBookmarkToGroupUseCase: MoveBookmarkToGroupUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val refetchBookmarkUseCase: RefetchBookmarkUseCase,
    private val cacheBookmarkFaviconUseCase: CacheBookmarkFaviconUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val syncGroupsUseCase: SyncGroupsUseCase
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>(Screen.GroupDetail.ARG_GROUP_ID) ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadGroup()
        observeBookmarks()
        observeGroups()
    }

    private fun loadGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val group = groupRepository.getGroupById(groupId)
            _uiState.update { it.copy(group = group, isLoading = false) }
            syncGroupsUseCase()
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            getBookmarksUseCase.byGroup(groupId).collect { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
        }
    }

    private fun observeGroups() {
        viewModelScope.launch {
            getGroupsUseCase().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    fun updateGroup(name: String, color: String?) {
        viewModelScope.launch {
            when (val result = updateGroupUseCase(groupId, name, color, null)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(group = result.data) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun moveBookmarkToGroup(bookmarkId: String, targetGroupId: String?) {
        viewModelScope.launch {
            when (val result = moveBookmarkToGroupUseCase(bookmarkId, targetGroupId)) {
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun deleteBookmark(bookmarkId: String) {
        viewModelScope.launch {
            when (val result = deleteBookmarkUseCase(bookmarkId)) {
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun refetchBookmark(bookmarkId: String) {
        viewModelScope.launch {
            when (val result = refetchBookmarkUseCase(bookmarkId)) {
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun cacheBookmarkFavicon(bookmarkId: String, faviconUrl: String) {
        viewModelScope.launch {
            cacheBookmarkFaviconUseCase(bookmarkId, faviconUrl)
        }
    }

    fun createGroupAndMoveBookmark(bookmarkId: String, groupName: String, groupColor: String?) {
        viewModelScope.launch {
            when (val result = createGroupUseCase(groupName, groupColor)) {
                is NetworkResult.Success -> {
                    val newGroup = result.data
                    moveBookmarkToGroup(bookmarkId, newGroup.id)
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}