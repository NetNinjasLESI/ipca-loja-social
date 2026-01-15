package com.ipca.lojasocial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipca.lojasocial.domain.model.Result
import com.ipca.lojasocial.domain.model.User
import com.ipca.lojasocial.domain.usecase.auth.GetCurrentUserUseCase
import com.ipca.lojasocial.domain.usecase.auth.LogoutUseCase
import com.ipca.lojasocial.domain.usecase.auth.SignInUseCase
import com.ipca.lojasocial.domain.usecase.notification.SubscribeToTopicsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Login Screen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val currentUser: User? = null
)

/**
 * ViewModel for Login Screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val subscribeToTopicsUseCase: SubscribeToTopicsUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    /**
     * Check if there's already an authenticated user
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is Result.Success -> {
                    result.data?.let { user ->
                        // Subscribe to topics when user is already logged in
                        subscribeToTopicsUseCase(user.role)

                        _uiState.value = _uiState.value.copy(
                            currentUser = user,
                            isSuccess = true
                        )
                    }
                }
                is Result.Error -> {
                    // User not authenticated, stay on login screen
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Update email field
     */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    /**
     * Update password field
     */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    /**
     * Perform sign in
     */
    fun signIn() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = signInUseCase(email, password)) {
                is Result.Success -> {
                    // Subscribe to notification topics based on user role
                    subscribeToTopicsUseCase(result.data.role)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        currentUser = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: result.exception.message ?: "Erro ao fazer login"
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Fazer logout
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (logoutUseCase()) {
                is Result.Success -> {
                    _uiState.value = LoginUiState() // Reset to initial state
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro ao fazer logout"
                    )
                }
                else -> {}
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
