package com.sayeedjoy.linkarena.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sayeedjoy.linkarena.domain.model.ThemeMode
import com.sayeedjoy.linkarena.domain.repository.AuthRepository
import com.sayeedjoy.linkarena.domain.repository.ThemePreferencesRepository
import com.sayeedjoy.linkarena.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userEmail: String? = null,
    val userName: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            combine(
                authRepository.userEmail,
                authRepository.userName,
                themePreferencesRepository.themeMode
            ) { email, name, themeMode ->
                Triple(email, name, themeMode)
            }.collect { (email, name, themeMode) ->
                _uiState.value = _uiState.value.copy(
                    userEmail = email,
                    userName = name,
                    themeMode = themeMode
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun onThemeModeChange(themeMode: ThemeMode) {
        viewModelScope.launch {
            themePreferencesRepository.setThemeMode(themeMode)
        }
    }
}
