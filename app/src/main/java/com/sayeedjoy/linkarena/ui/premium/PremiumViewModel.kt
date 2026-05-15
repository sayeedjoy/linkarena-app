package com.sayeedjoy.linkarena.ui.premium

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PremiumUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PremiumViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()
}
