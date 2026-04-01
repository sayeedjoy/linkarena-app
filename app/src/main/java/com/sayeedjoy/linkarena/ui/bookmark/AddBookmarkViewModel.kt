package com.sayeedjoy.linkarena.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.CreateBookmarkUseCase
import com.sayeedjoy.linkarena.domain.usecase.bookmarks.FetchUrlMetadataUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.CreateGroupUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.SyncGroupsUseCase
import com.sayeedjoy.linkarena.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddBookmarkUiState(
    val url: String = "",
    val title: String = "",
    val description: String = "",
    val faviconUrl: String? = null,
    val selectedGroupId: String? = null,
    val groups: List<Group> = emptyList(),
    val isFetchingMetadata: Boolean = false,
    val isCreatingGroup: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AddBookmarkViewModel @Inject constructor(
    private val createBookmarkUseCase: CreateBookmarkUseCase,
    private val fetchUrlMetadataUseCase: FetchUrlMetadataUseCase,
    private val groupRepository: GroupRepository,
    private val createGroupUseCase: CreateGroupUseCase,
    private val syncGroupsUseCase: SyncGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookmarkUiState())
    val uiState: StateFlow<AddBookmarkUiState> = _uiState.asStateFlow()
    private var metadataFetchJob: Job? = null
    private var lastMetadataLookupInput: String? = null

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
        _uiState.value = _uiState.value.copy(url = url, faviconUrl = null, error = null)
        scheduleAutoMetadataFetch(url)
    }

    fun prefillUrlIfEmpty(url: String) {
        if (_uiState.value.url.isBlank()) {
            _uiState.value = _uiState.value.copy(url = url.trim(), faviconUrl = null, error = null)
            scheduleAutoMetadataFetch(url)
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

    fun createGroup(name: String, color: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingGroup = true, error = null)
            when (val result = createGroupUseCase(name, color)) {
                is NetworkResult.Success -> {
                    val updatedGroups = (_uiState.value.groups + result.data).distinctBy { it.id }
                    _uiState.value = _uiState.value.copy(
                        groups = updatedGroups,
                        selectedGroupId = result.data.id,
                        isCreatingGroup = false
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingGroup = false,
                        error = result.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isCreatingGroup = true)
                }
            }
        }
    }

    fun createBookmark() {
        viewModelScope.launch {
            metadataFetchJob?.cancel()
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

    private fun scheduleAutoMetadataFetch(url: String) {
        metadataFetchJob?.cancel()

        val input = url.trim()
        if (!looksFetchableUrlInput(input) || input == lastMetadataLookupInput) {
            _uiState.value = _uiState.value.copy(isFetchingMetadata = false)
            return
        }

        metadataFetchJob = viewModelScope.launch {
            delay(600)
            val latestInput = _uiState.value.url.trim()
            if (latestInput != input || !looksFetchableUrlInput(latestInput)) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(isFetchingMetadata = true)
            lastMetadataLookupInput = latestInput

            when (val result = fetchUrlMetadataUseCase(latestInput)) {
                is NetworkResult.Success -> {
                    val metadata = result.data
                    val latest = _uiState.value
                    _uiState.value = latest.copy(
                        url = metadata.normalizedUrl,
                        title = latest.title.ifBlank { metadata.title.orEmpty() },
                        description = latest.description.ifBlank { metadata.description.orEmpty() },
                        faviconUrl = metadata.faviconUrl,
                        isFetchingMetadata = false
                    )
                }

                is NetworkResult.Error -> {
                    // Keep this silent for auto-fetch; users can still save manually.
                    _uiState.value = _uiState.value.copy(isFetchingMetadata = false)
                }

                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isFetchingMetadata = true)
                }
            }
        }
    }

    private fun looksFetchableUrlInput(input: String): Boolean {
        if (input.isBlank()) return false
        if (input.contains(' ')) return false
        return input.startsWith("http://", ignoreCase = true) ||
            input.startsWith("https://", ignoreCase = true) ||
            input.startsWith("www.", ignoreCase = true) ||
            input.contains('.')
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
