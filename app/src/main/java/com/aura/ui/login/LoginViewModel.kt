package com.aura.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aura.data.api.RetrofitInstance
import com.aura.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

// Le Repository est injecté en paramètre, le ViewModel ne sait pas que c'est Retrofit derrière
class LoginViewModel(private val repository: AuraRepository) : ViewModel() {

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")

    val isLoginButtonEnabled: StateFlow<Boolean> = combine(_email, _password) { email, password ->
        email.isNotBlank() && password.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChanged(email: String) { _email.value = email }
    fun onPasswordChanged(password: String) { _password.value = password }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                // On appelle le Repository, pas Retrofit directement
                val granted = repository.login(identifier, password)

                if (granted) {
                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.Error("Identifiants incorrects")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Erreur réseau : ${e.message}")
            }
        }
    }
}

class LoginViewModelFactory: ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        // On construit la chaîne complète Factory -> Repository -> API
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(AuraRepository(RetrofitInstance.api)) as T
    }
}