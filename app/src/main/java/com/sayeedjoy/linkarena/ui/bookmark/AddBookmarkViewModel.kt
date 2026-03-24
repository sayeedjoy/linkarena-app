package com.sayeedjoy.linkarena.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.CreateBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.SyncGroupsUseCase
import com.sayeedjoy.linkarena.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddBookmarkUiState(
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val selectedGroupId: String? = null,
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddBookmarkViewModel @Inject constructor(
    private val createBookmarkUseCase: CreateBookmarkUseCase,
    private val groupRepository: GroupRepository,
    private val syncGroupsUseCase: SyncGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookmarkUiState())
    val uiState: StateFlow<AddBookmarkUiState> = _uiState.asStateFlow()

    init {
        observeGroups()
        syncGroups()
    }

    private fun observeGroups() {
        viewModelScope.launch {
            groupRepository.getGroups().collect { groups ->
                _uiState.value = _uiState.value.copy(groups = groups)
            }
        }
    }

    private fun syncGroups() {
        viewModelScope.launch {
            syncGroupsUseCase()
        }
    }

    fun onUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(url = url, error = null)
    }

    fun prefillUrlIfEmpty(url: String) {
        if (_uiState.value.url.isBlank()) {
            _uiState.value = _uiState.value.copy(url = url.trim(), error = null)
        }
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

    fun createBookmark() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = createBookmarkUseCase(
                _uiState.value.url,
                _uiState.value.title.ifBlank { null },
                _uiState.value.description.ifBlank { null },
                _uiState.value.selectedGroupId
            )) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
