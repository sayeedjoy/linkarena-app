package com.sayeedjoy.linkarena.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.usecase.groups.CreateGroupUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.DeleteGroupUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.GetGroupsUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.SyncGroupsUseCase
import com.sayeedjoy.linkarena.domain.usecase.groups.UpdateGroupUseCase
import com.sayeedjoy.linkarena.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val syncGroupsUseCase: SyncGroupsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        observeGroups()
        syncGroups()
    }

    private fun observeGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getGroupsUseCase().collect { groups ->
                _uiState.value = _uiState.value.copy(groups = groups, isLoading = false)
            }
        }
    }

    private fun syncGroups() {
        viewModelScope.launch {
            when (val result = syncGroupsUseCase()) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun createGroup(name: String, color: String?) {
        viewModelScope.launch {
            when (val result = createGroupUseCase(name, color)) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun updateGroup(id: String, name: String?, color: String?, order: Int?) {
        viewModelScope.launch {
            when (val result = updateGroupUseCase(id, name, color, order)) {
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun deleteGroup(id: String) {
        viewModelScope.launch {
            when (val result = deleteGroupUseCase(id)) {
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
