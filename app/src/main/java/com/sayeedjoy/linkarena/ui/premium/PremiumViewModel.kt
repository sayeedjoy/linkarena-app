package com.sayeedjoy.linkarena.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.data.remote.dto.PlanItemDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val plans: List<PlanItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val api: LinkArenaApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        fetchPlans()
    }

    fun fetchPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getPlans()
                if (response.isSuccessful) {
                    val plans = response.body()?.plans?.sortedBy { it.sortOrder } ?: emptyList()
                    _uiState.update { it.copy(plans = plans, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Could not load plans") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Network error") }
            }
        }
    }
}
