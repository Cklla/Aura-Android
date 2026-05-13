package com.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aura.data.api.RetrofitInstance
import com.aura.data.repository.AuraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Les quatre états possibles de l'écran principal
sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val balance: Double): HomeUiState() // on transporte la balance
    data class Error(val message: String) : HomeUiState()

}

class HomeViewModel(private val repository: AuraRepository) : ViewModel() {

    // L'état de l'UI, commence à idle (rien n'est chargé)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState : StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Appelé depuis HomeActivity quand on a le userId
    fun loadBalance(userId: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val balance = repository.getAccounts(userId)
                _uiState.value = HomeUiState.Success(balance)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}

// La Factory suit le même pattern que LoginViewModelFactory
class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(AuraRepository(RetrofitInstance.api)) as T
    }
}